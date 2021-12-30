package ch.qos.logback.core.model.processor;

import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.ImcplicitActionDataForBasicProperty;
import ch.qos.logback.core.joran.action.ImplicitActionDataBase;
import ch.qos.logback.core.joran.action.ImplicitActionDataForComplexProperty;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.ComponentModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class ImplicitModelHandler extends ModelHandlerBase {

	private final BeanDescriptionCache beanDescriptionCache;

	static final String  PARENT_PROPPERTY_KEY = "parent";
	
	
	boolean inError = false;

	public ImplicitModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
		super(context);
		this.beanDescriptionCache = beanDescriptionCache;
	}

	protected Class<? extends ImplicitModel> getSupportedModelClass() {
		return ImplicitModel.class;
	}

	static public ImplicitModelHandler makeInstance(Context context, InterpretationContext ic) {
		return new ImplicitModelHandler(context, ic.getBeanDescriptionCache());
	}
	
	@Override
	public void handle(InterpretationContext intercon, Model model) {

		ImplicitModel implicitModel = (ImplicitModel) model;

		// calling intercon.peekObject with an empty stack will throw an exception
		if (intercon.isObjectStackEmpty()) {
			inError = true;
			return;
		}
		String nestedElementTagName = implicitModel.getTag();

		Object o = intercon.peekObject();
		PropertySetter parentBean = new PropertySetter(beanDescriptionCache, o);
		parentBean.setContext(context);

		AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);

		Stack<ImplicitActionDataBase> actionDataStack = intercon.getImplcitActionDataStack();

		switch (aggregationType) {
		case NOT_FOUND:
			addWarn("Ignoring unkown property ["+nestedElementTagName+"] in ["+o.getClass().getName()+"]");
			inError = true;
			return;
		case AS_BASIC_PROPERTY:
		case AS_BASIC_PROPERTY_COLLECTION:
			ImcplicitActionDataForBasicProperty adBasicProperty = new ImcplicitActionDataForBasicProperty(parentBean,
					aggregationType, nestedElementTagName);
			actionDataStack.push(adBasicProperty);
			doBasicProperty(intercon, model, adBasicProperty);
			return;
		// we only push action data if NestComponentIA is applicable
		case AS_COMPLEX_PROPERTY_COLLECTION:
		case AS_COMPLEX_PROPERTY:
			ImplicitActionDataForComplexProperty adComplex = new ImplicitActionDataForComplexProperty(parentBean,
					aggregationType, nestedElementTagName);
			actionDataStack.push(adComplex);
			doComplex(intercon, implicitModel, adComplex);
			return;
		default:
			addError("PropertySetter.computeAggregationType returned " + aggregationType);
			return;
		}

	}

	void doBasicProperty(InterpretationContext interpretationContext, Model model,
			ImcplicitActionDataForBasicProperty actionData) {
		String finalBody = interpretationContext.subst(model.getBodyText());
		// get the action data object pushed in isApplicable() method call
		// IADataForBasicProperty actionData = (IADataForBasicProperty)
		// actionDataStack.peek();
		switch (actionData.aggregationType) {
		case AS_BASIC_PROPERTY:
			actionData.parentBean.setProperty(actionData.propertyName, finalBody);
			break;
		case AS_BASIC_PROPERTY_COLLECTION:
			actionData.parentBean.addBasicProperty(actionData.propertyName, finalBody);
			break;
		default:
			addError("Unexpected aggregationType " + actionData.aggregationType);
		}
	}

	public void doComplex(InterpretationContext interpretationContext, ComponentModel componentModel,
			ImplicitActionDataForComplexProperty actionData) {

		String className = componentModel.getClassName();
		// perform variable name substitution
		String substClassName = interpretationContext.subst(className);
		
		String fqcn = interpretationContext.getImport(substClassName);
		
		
		Class<?> componentClass = null;
		try {

			if (!OptionHelper.isNullOrEmpty(fqcn)) {
				componentClass = Loader.loadClass(fqcn, context);
			} else {
				// guess class name via implicit rules
				PropertySetter parentBean = actionData.parentBean;
				componentClass = parentBean.getClassNameViaImplicitRules(actionData.propertyName,
						actionData.getAggregationType(), interpretationContext.getDefaultNestedComponentRegistry());
			}

			if (componentClass == null) {
				actionData.inError = true;
				String errMsg = "Could not find an appropriate class for property [" + componentModel.getTag() + "]";
				addError(errMsg);
				return;
			}

			if (OptionHelper.isNullOrEmpty(fqcn)) {
				addInfo("Assuming default type [" + componentClass.getName() + "] for [" + componentModel.getTag()
						+ "] property");
			}

			actionData.setNestedComplexProperty(componentClass.getConstructor().newInstance());

			// pass along the repository
			if (actionData.getNestedComplexProperty() instanceof ContextAware) {
				((ContextAware) actionData.getNestedComplexProperty()).setContext(this.context);
			}
			// addInfo("Pushing component [" + localName
			// + "] on top of the object stack.");
			interpretationContext.pushObject(actionData.getNestedComplexProperty());

		} catch (Exception oops) {
			actionData.inError = true;
			String msg = "Could not create component [" + componentModel.getTag() + "] of type [" + fqcn + "]";
			addError(msg, oops);
		}
	}

	@Override
	public void postHandle(InterpretationContext intercon, Model model) {
		if (inError) {
			return;
		}

		Stack<ImplicitActionDataBase> actionDataStack = intercon.getImplcitActionDataStack();
		ImplicitActionDataBase actionData = actionDataStack.pop();

		if (actionData instanceof ImplicitActionDataForComplexProperty) {
			postHandleComplex(intercon, model, actionData);
		}

	}

	private void postHandleComplex(InterpretationContext intercon, Model model, ImplicitActionDataBase actionData) {
		ImplicitActionDataForComplexProperty complexActionData = (ImplicitActionDataForComplexProperty) actionData;

		PropertySetter nestedBean = new PropertySetter(beanDescriptionCache,
				complexActionData.getNestedComplexProperty());
		nestedBean.setContext(context);

		// have the nested element point to its parent if possible
		if (nestedBean.computeAggregationType(PARENT_PROPPERTY_KEY) == AggregationType.AS_COMPLEX_PROPERTY) {
			nestedBean.setComplexProperty(PARENT_PROPPERTY_KEY, actionData.parentBean.getObj());
		}

		// start the nested complex property if it implements LifeCycle and is not
		// marked with a @NoAutoStart annotation
		Object nestedComplexProperty = complexActionData.getNestedComplexProperty();
		if (nestedComplexProperty instanceof LifeCycle
				&& NoAutoStartUtil.notMarkedWithNoAutoStart(nestedComplexProperty)) {
			((LifeCycle) nestedComplexProperty).start();
		}

		Object o = intercon.peekObject();

		if (o != complexActionData.getNestedComplexProperty()) {
			addError("The object on the top the of the stack is not the component pushed earlier.");
		} else {
			intercon.popObject();
			// Now let us attach the component
			switch (actionData.aggregationType) {
			case AS_COMPLEX_PROPERTY:
				actionData.parentBean.setComplexProperty(model.getTag(), complexActionData.getNestedComplexProperty());

				break;
			case AS_COMPLEX_PROPERTY_COLLECTION:
				actionData.parentBean.addComplexProperty(model.getTag(), complexActionData.getNestedComplexProperty());
				break;
			default:
				addError("Unexpected aggregationType " + actionData.aggregationType);
			}
		}
	}

}
