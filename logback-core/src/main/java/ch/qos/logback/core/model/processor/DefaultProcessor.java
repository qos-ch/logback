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

	public DefaultProcessor(Context context, InterpretationContext interpretationContext) {
		this.setContext(context);
		this.interpretationContext = interpretationContext;
	}

	public void addHandler(Class<? extends Model> modelClass, ModelFactoryMethod modelFactoryMethod) {
		modelClassToHandlerMap.put(modelClass, modelFactoryMethod);
	}

	public void addAnalyser(Class<? extends Model> modelClass, ModelHandlerBase handler) {
		modelClassToDependencyAnalyserMap.put(modelClass, handler);
	}

	private void traversalLoop(TraverseMethod traverseMethod, Model model, ModelFiler modelfFilter, String phaseName) {
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

	public void setPhaseOneFilter(ModelFiler phaseOneFilter) {
		this.phaseOneFilter = phaseOneFilter;
	}

	public void setPhaseTwoFilter(ModelFiler phaseTwoFilter) {
		this.phaseTwoFilter = phaseTwoFilter;
	}

	protected void analyseDependencies(Model model) {
		ModelHandlerBase handler = modelClassToDependencyAnalyserMap.get(model.getClass());

		if (handler != null) {
			try {
				handler.handle(interpretationContext, model);
			} catch (ModelHandlerException e) {
				addError("Failed to traverse model " + model.getTag(), e);
			}
		}

		for (Model m : model.getSubModels()) {
			analyseDependencies(m);
		}
		if (handler != null) {
			try {
				handler.postHandle(interpretationContext, model);
			} catch (ModelHandlerException e) {
				addError("Failed to invole postHandle on model " + model.getTag(), e);
			}
		}
	}

	static final int DENIED = -1;

	private ModelHandlerBase createHandler(Model model) {
		ModelFactoryMethod modelFactoryMethod = modelClassToHandlerMap.get(model.getClass());

		if (modelFactoryMethod == null) {
			addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag() + " at line "
					+ model.getLineNumber());
			return null;
		}

		ModelHandlerBase handler = modelFactoryMethod.make(context, interpretationContext);
		if (handler == null)
			return null;
		if (!handler.isSupportedModelType(model)) {
			addWarn("Handler [" + handler.getClass() + "] does not support " + model.idString());
			return null;
		}
		return handler;
	}

	protected int mainTraverse(Model model, ModelFiler modelFiler) {

		FilterReply filterReply = modelFiler.decide(model);
		if (filterReply == FilterReply.DENY)
			return DENIED;

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

			if (!model.isSkipped()) {
				for (Model m : model.getSubModels()) {
					count += mainTraverse(m, modelFiler);
				}
			}
			if (handler != null) {
				handler.postHandle(interpretationContext, model);
			}
		} catch (ModelHandlerException e) {
			addError("Failed to traverse model " + model.getTag(), e);
		}
		return count;
	}

	protected int secondPhaseTraverse(Model model, ModelFiler modelFilter) {

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
					handler.handle(interpretationContext, model);
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
				handler.postHandle(interpretationContext, model);
			}
		} catch (ModelHandlerException e) {
			addError("Failed to traverse model " + model.getTag(), e);
		}
		return count;
	}

	private boolean dependencyIsADirectSubmodel(Model model) {
		List<String> dependecyList = this.interpretationContext.getDependencies(model);
		if (dependecyList == null || dependecyList.isEmpty()) {
			return false;
		}
		for (Model submodel : model.getSubModels()) {
			if (submodel instanceof NamedComponentModel) {
				NamedComponentModel namedComponentModel = (NamedComponentModel) submodel;
				String subModelName = namedComponentModel.getName();
				if (dependecyList.contains(subModelName)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean allDependenciesStarted(Model model) {
		List<String> dependecyList = this.interpretationContext.getDependencies(model);
		if (dependecyList == null || dependecyList.isEmpty()) {
			return true;
		}
		for (String name : dependecyList) {
			boolean isStarted = interpretationContext.isNamedDependencyStarted(name);
			if (isStarted == false) {
				return isStarted;
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
