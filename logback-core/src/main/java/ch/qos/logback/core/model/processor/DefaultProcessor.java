package ch.qos.logback.core.model.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelFactoryMethod;
import ch.qos.logback.core.model.NamedComponentModel;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

public class DefaultProcessor extends ContextAwareBase {

    interface TraverseMethod {
        int traverse(Model model, ModelFiler modelFiler);
    }


    final InterpretationContext interpretationContext;
    final HashMap<Class<? extends Model>, ModelFactoryMethod> modelClassToHandlerMap = new HashMap<>();
    final HashMap<Class<? extends Model>, ModelHandlerBase> modelClassToDependencyAnalyserMap = new HashMap<>();

    ModelFiler phaseOneFilter = new AllowAllModelFilter();
    ModelFiler phaseTwoFilter = new DenyAllModelFilter();

    public DefaultProcessor(final Context context, final InterpretationContext interpretationContext) {
        setContext(context);
        this.interpretationContext = interpretationContext;
    }

    public void addHandler(final Class<? extends Model> modelClass, final ModelFactoryMethod modelFactoryMethod) {
        modelClassToHandlerMap.put(modelClass, modelFactoryMethod);
    }

    public void addAnalyser(final Class<? extends Model> modelClass, final ModelHandlerBase handler) {
        modelClassToDependencyAnalyserMap.put(modelClass, handler);
    }

    private void traversalLoop(final TraverseMethod traverseMethod, final Model model, final ModelFiler modelfFilter, final String phaseName) {
        final int LIMIT = 3;
        for (int i = 0; i < LIMIT; i++) {
            final int handledModelCount = traverseMethod.traverse(model, modelfFilter);
            if (handledModelCount == 0) {
                break;
            }
        }
    }

    public void process(final Model model) {

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
        interpretationContext.popObject();
    }

    private void initialObjectPush() {
        interpretationContext.pushObject(context);
    }

    public ModelFiler getPhaseOneFilter() {
        return phaseOneFilter;
    }

    public ModelFiler getPhaseTwoFilter() {
        return phaseTwoFilter;
    }

    public void setPhaseOneFilter(final ModelFiler phaseOneFilter) {
        this.phaseOneFilter = phaseOneFilter;
    }

    public void setPhaseTwoFilter(final ModelFiler phaseTwoFilter) {
        this.phaseTwoFilter = phaseTwoFilter;
    }

    protected void analyseDependencies(final Model model) {
        final ModelHandlerBase handler = modelClassToDependencyAnalyserMap.get(model.getClass());

        if (handler != null) {
            try {
                handler.handle(interpretationContext, model);
            } catch (final ModelHandlerException e) {
                addError("Failed to traverse model " + model.getTag(), e);
            }
        }

        for (final Model m : model.getSubModels()) {
            analyseDependencies(m);
        }
        if (handler != null) {
            try {
                handler.postHandle(interpretationContext, model);
            } catch (final ModelHandlerException e) {
                addError("Failed to invole postHandle on model " + model.getTag(), e);
            }
        }
    }

    static final int DENIED = -1;

    private ModelHandlerBase createHandler(final Model model) {
        final ModelFactoryMethod modelFactoryMethod  = modelClassToHandlerMap.get(model.getClass());

        if (modelFactoryMethod == null) {
            addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag() + " at line "
                            + model.getLineNumber());
            return null;
        }

        final ModelHandlerBase handler = modelFactoryMethod.make(context, interpretationContext);
        if (handler == null) {
            return null;
        }
        if (!handler.isSupportedModelType(model)) {
            addWarn("Handler [" + handler.getClass() + "] does not support " + model.idString());
            return null;
        }
        return handler;
    }

    protected int mainTraverse(final Model model, final ModelFiler modelFiler) {

        final FilterReply filterReply = modelFiler.decide(model);
        if (filterReply == FilterReply.DENY) {
            return DENIED;
        }

        int count = 0;

        try {
            ModelHandlerBase handler = null;
            if (model.isUnhandled()) {
                handler = createHandler(model);
                if (handler != null) {
                    handler.handle(interpretationContext, model);
                    model.markAsHandled();
                    count++;
                }
            }
            // recurse into submodels handled or not

            for (final Model m : model.getSubModels()) {
                count += mainTraverse(m, modelFiler);
            }
            if (handler != null) {
                handler.postHandle(interpretationContext, model);
            }
        } catch (final ModelHandlerException e) {
            addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    protected int secondPhaseTraverse(final Model model, final ModelFiler modelFilter) {

        final FilterReply filterReply = modelFilter.decide(model);
        if (filterReply == FilterReply.DENY) {
            return 0;
        }

        int count = 0;

        try {

            final boolean allDependenciesStarted = allDependenciesStarted(model);

            ModelHandlerBase handler = null;
            if (model.isUnhandled() && allDependenciesStarted) {
                handler = createHandler(model);
                if (handler != null) {
                    handler.handle(interpretationContext, model);
                    model.markAsHandled();
                    count++;
                }
            }

            if (!allDependenciesStarted && !dependencyIsADirectSubmodel(model)) {
                return count;
            }

            for (final Model m : model.getSubModels()) {
                count += secondPhaseTraverse(m, modelFilter);
            }
            if (handler != null) {
                handler.postHandle(interpretationContext, model);
            }
        } catch (final ModelHandlerException e) {
            addError("Failed to traverse model " + model.getTag(), e);
        }
        return count;
    }

    private boolean dependencyIsADirectSubmodel(final Model model) {
        final List<String> dependecyList = interpretationContext.getDependencies(model);
        if (dependecyList == null || dependecyList.isEmpty()) {
            return false;
        }
        for (final Model submodel : model.getSubModels()) {
            if (submodel instanceof NamedComponentModel) {
                final NamedComponentModel namedComponentModel = (NamedComponentModel) submodel;
                final String subModelName = namedComponentModel.getName();
                if (dependecyList.contains(subModelName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean allDependenciesStarted(final Model model) {
        final List<String> dependecyList = interpretationContext.getDependencies(model);
        if (dependecyList == null || dependecyList.isEmpty()) {
            return true;
        }
        for (final String name : dependecyList) {
            final boolean isStarted = interpretationContext.isNamedDependencyStarted(name);
            if (!isStarted) {
                return isStarted;
            }
        }
        return true;
    }

    ModelHandlerBase instantiateHandler(final Class<? extends ModelHandlerBase> handlerClass) {
        try {
            final Constructor<? extends ModelHandlerBase> commonConstructor = getWithContextConstructor(handlerClass);
            if (commonConstructor != null) {
                return commonConstructor.newInstance(context);
            }
            final Constructor<? extends ModelHandlerBase> constructorWithBDC = getWithContextAndBDCConstructor(handlerClass);
            if (constructorWithBDC != null) {
                return constructorWithBDC.newInstance(context, interpretationContext.getBeanDescriptionCache());
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
                    final Class<? extends ModelHandlerBase> handlerClass) {
        try {
            return handlerClass.getConstructor(Context.class);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    private Constructor<? extends ModelHandlerBase> getWithContextAndBDCConstructor(
                    final Class<? extends ModelHandlerBase> handlerClass) {
        try {
            return handlerClass.getConstructor(Context.class,
                            BeanDescriptionCache.class);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }


}
