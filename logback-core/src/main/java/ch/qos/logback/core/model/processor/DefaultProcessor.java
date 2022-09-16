/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelHandlerFactoryMethod;
import ch.qos.logback.core.model.NamedComponentModel;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

/**
 * DefaultProcessor traverses the Model produced at an earlier step and performs actual
 * configuration of logback according to the handlers it was given.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.0
 */
public class DefaultProcessor extends ContextAwareBase {

    interface TraverseMethod {
        int traverse(Model model, ModelFilter modelFiler);
    }

    final protected ModelInterpretationContext mic;
    final HashMap<Class<? extends Model>, ModelHandlerFactoryMethod> modelClassToHandlerMap = new HashMap<>();
    final HashMap<Class<? extends Model>, Supplier<ModelHandlerBase>> modelClassToDependencyAnalyserMap = new HashMap<>();

    ChainedModelFilter phaseOneFilter = new ChainedModelFilter();
    ChainedModelFilter phaseTwoFilter = new ChainedModelFilter();

    public DefaultProcessor(Context context, ModelInterpretationContext mic) {
        this.setContext(context);
        this.mic = mic;
    }

    public void addHandler(Class<? extends Model> modelClass, ModelHandlerFactoryMethod modelFactoryMethod) {

        modelClassToHandlerMap.put(modelClass, modelFactoryMethod);

        ProcessingPhase phase = determineProcessingPhase(modelClass);
        switch (phase) {
            case FIRST:
                getPhaseOneFilter().allow(modelClass);
                break;
            case SECOND:
                getPhaseTwoFilter().allow(modelClass);
                break;
            default:
                throw new IllegalArgumentException("unexpected value " + phase + " for model class " + modelClass.getName());
        }
    }

    private ProcessingPhase determineProcessingPhase(Class<? extends Model> modelClass) {

        PhaseIndicator phaseIndicator = modelClass.getAnnotation(PhaseIndicator.class);
        if (phaseIndicator == null) {
            return ProcessingPhase.FIRST;
        }

        ProcessingPhase phase = phaseIndicator.phase();
        return phase;
    }

    public void addAnalyser(Class<? extends Model> modelClass, Supplier<ModelHandlerBase> analyserSupplier) {
        modelClassToDependencyAnalyserMap.put(modelClass, analyserSupplier);
    }

    private void traversalLoop(TraverseMethod traverseMethod, Model model, ModelFilter modelfFilter, String phaseName) {
        int LIMIT = 3;
        for (int i = 0; i < LIMIT; i++) {
            int handledModelCount = traverseMethod.traverse(model, modelfFilter);
            if (handledModelCount == 0)
                break;
        }
    }

    public void process(Model model) {

        if (model == null) {
            addError("Expecting non null model to process");
            return;
        }
        initialObjectPush();

        mainTraverse(model, getPhaseOneFilter());
        analyseDependencies(model);
        traversalLoop(this::secondPhaseTraverse, model, getPhaseTwoFilter(), "phase 2");

        addInfo("End of configuration.");
        finalObjectPop();
    }

    private void finalObjectPop() {
        mic.popObject();
    }

    private void initialObjectPush() {
        mic.pushObject(context);
    }

    public ChainedModelFilter getPhaseOneFilter() {
        return phaseOneFilter;
    }

    public ChainedModelFilter getPhaseTwoFilter() {
        return phaseTwoFilter;
    }


    protected void analyseDependencies(Model model) {
        Supplier<ModelHandlerBase> analyserSupplier = modelClassToDependencyAnalyserMap.get(model.getClass());

        ModelHandlerBase analyser = null;

        if (analyserSupplier != null) {
            analyser = analyserSupplier.get();
        }

        if (analyser != null && !model.isSkipped()) {
            callAnalyserHandleOnModel(model, analyser);
        }

        for (Model m : model.getSubModels()) {
            analyseDependencies(m);
        }

        if (analyser != null && !model.isSkipped()) {
            callAnalyserPostHandleOnModel(model, analyser);
        }
    }

    private void callAnalyserPostHandleOnModel(Model model, ModelHandlerBase analyser) {
        try {
            analyser.postHandle(mic, model);
        } catch (ModelHandlerException e) {
            addError("Failed to invoke postHandle on model " + model.getTag(), e);
        }
    }

    private void callAnalyserHandleOnModel(Model model, ModelHandlerBase analyser) {
        try {
            analyser.handle(mic, model);
        } catch (ModelHandlerException e) {
            addError("Failed to traverse model " + model.getTag(), e);
        }
    }

    static final int DENIED = -1;

    private ModelHandlerBase createHandler(Model model) {
        ModelHandlerFactoryMethod modelFactoryMethod = modelClassToHandlerMap.get(model.getClass());

        if (modelFactoryMethod == null) {
            addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag() + " at line "
                    + model.getLineNumber());
            return null;
        }

        ModelHandlerBase handler = modelFactoryMethod.make(context, mic);
        if (handler == null)
            return null;
        if (!handler.isSupportedModelType(model)) {
            addWarn("Handler [" + handler.getClass() + "] does not support " + model.idString());
            return null;
        }
        return handler;
    }

    protected int mainTraverse(Model model, ModelFilter modelFiler) {

        FilterReply filterReply = modelFiler.decide(model);
        if (filterReply == FilterReply.DENY)
            return DENIED;

        int count = 0;

        try {
            ModelHandlerBase handler = null;
            boolean unhandled = model.isUnhandled();

            if (unhandled) {
                handler = createHandler(model);
                if (handler != null) {
                    handler.handle(mic, model);
                    model.markAsHandled();
                    count++;
                }
            }
            // recurse into submodels handled or not
            if (!model.isSkipped()) {
                for (Model m : model.getSubModels()) {
                    count += mainTraverse(m, modelFiler);
                }
            }

            if (unhandled && handler != null) {
                handler.postHandle(mic, model);
            }
        } catch (ModelHandlerException e) {
            addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    protected int secondPhaseTraverse(Model model, ModelFilter modelFilter) {

        FilterReply filterReply = modelFilter.decide(model);
        if (filterReply == FilterReply.DENY) {
            return 0;
        }

        int count = 0;

        try {

            boolean allDependenciesStarted = allDependenciesStarted(model);

            ModelHandlerBase handler = null;
            if (model.isUnhandled() && allDependenciesStarted) {
                handler = createHandler(model);
                if (handler != null) {
                    handler.handle(mic, model);
                    model.markAsHandled();
                    count++;
                }
            }

            if (!allDependenciesStarted && !dependencyIsADirectSubmodel(model)) {
                return count;
            }

            if (!model.isSkipped()) {
                for (Model m : model.getSubModels()) {
                    count += secondPhaseTraverse(m, modelFilter);
                }
            }
            if (handler != null) {
                handler.postHandle(mic, model);
            }
        } catch (ModelHandlerException e) {
            addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    private boolean dependencyIsADirectSubmodel(Model model) {
        List<String> dependecyNames = this.mic.getDependeeNamesForModel(model);
        if (dependecyNames == null || dependecyNames.isEmpty()) {
            return false;
        }
        for (Model submodel : model.getSubModels()) {
            if (submodel instanceof NamedComponentModel) {
                NamedComponentModel namedComponentModel = (NamedComponentModel) submodel;
                String subModelName = namedComponentModel.getName();
                if (dependecyNames.contains(subModelName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean allDependenciesStarted(Model model) {
        List<String> dependencyNames = mic.getDependeeNamesForModel(model);

        if (dependencyNames == null || dependencyNames.isEmpty()) {
            return true;
        }
        for (String name : dependencyNames) {
            boolean isStarted = mic.isNamedDependeeStarted(name);
            if (isStarted == false) {
                return false;
            }
        }
        return true;
    }

    ModelHandlerBase instantiateHandler(Class<? extends ModelHandlerBase> handlerClass) {
        try {
            Constructor<? extends ModelHandlerBase> commonConstructor = getWithContextConstructor(handlerClass);
            if (commonConstructor != null) {
                return commonConstructor.newInstance(context);
            }
            Constructor<? extends ModelHandlerBase> constructorWithBDC = getWithContextAndBDCConstructor(handlerClass);
            if (constructorWithBDC != null) {
                return constructorWithBDC.newInstance(context, mic.getBeanDescriptionCache());
            }
            addError("Failed to find suitable constructor for class [" + handlerClass + "]");
            return null;
        } catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException
                | InvocationTargetException e1) {
            addError("Failed to instantiate " + handlerClass);
            return null;
        }
    }

    private Constructor<? extends ModelHandlerBase> getWithContextConstructor(
            Class<? extends ModelHandlerBase> handlerClass) {
        try {
            Constructor<? extends ModelHandlerBase> constructor = handlerClass.getConstructor(Context.class);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Constructor<? extends ModelHandlerBase> getWithContextAndBDCConstructor(
            Class<? extends ModelHandlerBase> handlerClass) {
        try {
            Constructor<? extends ModelHandlerBase> constructor = handlerClass.getConstructor(Context.class,
                    BeanDescriptionCache.class);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
