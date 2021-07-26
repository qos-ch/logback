package ch.qos.logback.core.model.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

public class DefaultProcessor extends ContextAwareBase {

	interface TraverseMethod {
		int traverse(Model model, ModelFiler modelFiler);
	}

	final InterpretationContext interpretationContext;
	final HashMap<Class<? extends Model>, Class<? extends ModelHandlerBase>> modelClassToHandlerMap = new HashMap<>();
	final HashMap<Class<? extends Model>, ModelHandlerBase> modelClassToDependencyAnalyserMap = new HashMap<>();

	ModelFiler phaseOneFilter = new AllowAllModelFilter();
	ModelFiler phaseTwoFilter = new DenyAllModelFilter();

	public DefaultProcessor(Context context, InterpretationContext interpretationContext) {
		this.setContext(context);
		this.interpretationContext = interpretationContext;
	}

	public void addHandler(Class<? extends Model> modelClass, Class<? extends ModelHandlerBase> handlerClass) {
		modelClassToHandlerMap.put(modelClass, handlerClass);
	}

	public void addAnalyser(Class<? extends Model> modelClass, ModelHandlerBase handler) {
		modelClassToDependencyAnalyserMap.put(modelClass, handler);
	}

	private void traversalLoop(TraverseMethod traverseMethod, Model model, ModelFiler modelfFilter, String phaseName) {
		int LIMIT = 3;
		for (int i = 0; i < LIMIT; i++) {
			int handledModelCount = traverseMethod.traverse(model, modelfFilter);
			addInfo(phaseName + " handledModelCount=" + handledModelCount);
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

		// phaseOneTraverse();

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
	}

	static final int DENIED = -1;

	protected int mainTraverse(Model model, ModelFiler modelFiler) {

		FilterReply filterReply = modelFiler.decide(model);
		if (filterReply == FilterReply.DENY)
			return DENIED;

		Class<? extends ModelHandlerBase> handlerClass = modelClassToHandlerMap.get(model.getClass());

		if (handlerClass == null) {
			addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag() + " at line "
					+ model.getLineNumber());
			return 0;
		}

		ModelHandlerBase handler = instantiateHandler(handlerClass);
		int count = 0;

		try {

			if (!handler.isSupportedModelType(model)) {
				addWarn("Skipping processing for model " + model.idString());
				return count;
			}
			boolean handledHere = false;

			if (model.isUnhandled()) {
				handler.handle(interpretationContext, model);
				handledHere = true;
				model.markAsHandled();
				count++;
			}
			// recurse into submodels handled or not

			for (Model m : model.getSubModels()) {
				count += mainTraverse(m, modelFiler);
			}
			if (handledHere) {
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

		Class<? extends ModelHandlerBase> handlerClass = modelClassToHandlerMap.get(model.getClass());

		if (handlerClass == null) {
			addError("Can't handle model of type " + model.getClass() + "  with tag: " + model.getTag() + " at line "
					+ model.getLineNumber());
			return 0;
		}

		int count = 0;

		ModelHandlerBase handler = instantiateHandler(handlerClass);

		try {
			if (!handler.isSupportedModelType(model)) {
				addWarn("Skipping processing for model " + model.idString());
				return count;
			}

			boolean allDependenciesStarted = allDependenciesStarted(model);

			boolean handledHere = false;
			if (model.isUnhandled() && allDependenciesStarted) {
				handler.handle(interpretationContext, model);
				handledHere = true;
				model.markAsHandled();
				count++;
			}

			if(!allDependenciesStarted) {
				return count;
			}
			
			for (Model m: model.getSubModels()) {
				count += secondPhaseTraverse(m, modelFilter);
			}
			if (handledHere) {
				handler.postHandle(interpretationContext, model);
			}
		} catch (ModelHandlerException e) {
			addError("Failed to traverse model " + model.getTag(), e);
		}
		return count;
	}

	private boolean allDependenciesStarted(Model model) {
		List<String> dependecyList = this.interpretationContext.getDependencies(model);
		if (dependecyList == null || dependecyList.isEmpty()) {
			return true;
		}
		for(String name: dependecyList) {
			boolean isStarted = interpretationContext.isNamedDependencyStarted(name);
			if(isStarted == false) {
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
