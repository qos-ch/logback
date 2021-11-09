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

    public ImplicitModelHandler(final Context context, final BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    @Override
    protected Class<? extends ImplicitModel> getSupportedModelClass() {
        return ImplicitModel.class;
    }

    static public ImplicitModelHandler makeInstance(final Context context, final InterpretationContext ic) {
        return new ImplicitModelHandler(context, ic.getBeanDescriptionCache());
    }

    @Override
    public void handle(final InterpretationContext intercon, final Model model) {

        final ImplicitModel implicitModel = (ImplicitModel) model;

        // calling intercon.peekObject with an empty stack will throw an exception
        if (intercon.isObjectStackEmpty()) {
            inError = true;
            return;
        }
        final String nestedElementTagName = implicitModel.getTag();

        final Object o = intercon.peekObject();
        final PropertySetter parentBean = new PropertySetter(beanDescriptionCache, o);
        parentBean.setContext(context);

        final AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);

        final Stack<ImplicitActionDataBase> actionDataStack = intercon.getImplcitActionDataStack();

        switch (aggregationType) {
        case NOT_FOUND:
            addWarn("Ignoring unkown property ["+nestedElementTagName+"] in ["+o.getClass().getName()+"]");
            inError = true;
            return;
        case AS_BASIC_PROPERTY:
        case AS_BASIC_PROPERTY_COLLECTION:
            final ImcplicitActionDataForBasicProperty adBasicProperty = new ImcplicitActionDataForBasicProperty(parentBean,
                            aggregationType, nestedElementTagName);
            actionDataStack.push(adBasicProperty);
            doBasicProperty(intercon, model, adBasicProperty);
            return;
            // we only push action data if NestComponentIA is applicable
        case AS_COMPLEX_PROPERTY_COLLECTION:
        case AS_COMPLEX_PROPERTY:
            final ImplicitActionDataForComplexProperty adComplex = new ImplicitActionDataForComplexProperty(parentBean,
                            aggregationType, nestedElementTagName);
            actionDataStack.push(adComplex);
            doComplex(intercon, implicitModel, adComplex);
            return;
        default:
            addError("PropertySetter.computeAggregationType returned " + aggregationType);
        }

    }

    void doBasicProperty(final InterpretationContext interpretationContext, final Model model,
                    final ImcplicitActionDataForBasicProperty actionData) {
        final String finalBody = interpretationContext.subst(model.getBodyText());
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

    public void doComplex(final InterpretationContext interpretationContext, final ComponentModel componentModel,
                    final ImplicitActionDataForComplexProperty actionData) {

        String className = componentModel.getClassName();
        // perform variable name substitution
        className = interpretationContext.subst(className);

        Class<?> componentClass = null;
        try {

            if (!OptionHelper.isNullOrEmpty(className)) {
                componentClass = Loader.loadClass(className, context);
            } else {
                // guess class name via implicit rules
                final PropertySetter parentBean = actionData.parentBean;
                componentClass = parentBean.getClassNameViaImplicitRules(actionData.propertyName,
                                actionData.getAggregationType(), interpretationContext.getDefaultNestedComponentRegistry());
            }

            if (componentClass == null) {
                actionData.inError = true;
                final String errMsg = "Could not find an appropriate class for property [" + componentModel.getTag() + "]";
                addError(errMsg);
                return;
            }

            if (OptionHelper.isNullOrEmpty(className)) {
                addInfo("Assuming default type [" + componentClass.getName() + "] for [" + componentModel.getTag()
                + "] property");
            }

            actionData.setNestedComplexProperty(componentClass.getConstructor().newInstance());

            // pass along the repository
            if (actionData.getNestedComplexProperty() instanceof ContextAware) {
                ((ContextAware) actionData.getNestedComplexProperty()).setContext(context);
            }
            // addInfo("Pushing component [" + localName
            // + "] on top of the object stack.");
            interpretationContext.pushObject(actionData.getNestedComplexProperty());

        } catch (final Exception oops) {
            actionData.inError = true;
            final String msg = "Could not create component [" + componentModel.getTag() + "] of type [" + className + "]";
            addError(msg, oops);
        }
    }

    @Override
    public void postHandle(final InterpretationContext intercon, final Model model) {
        if (inError) {
            return;
        }

        final Stack<ImplicitActionDataBase> actionDataStack = intercon.getImplcitActionDataStack();
        final ImplicitActionDataBase actionData = actionDataStack.pop();

        if (actionData instanceof ImplicitActionDataForComplexProperty) {
            postHandleComplex(intercon, model, actionData);
        }

    }

    private void postHandleComplex(final InterpretationContext intercon, final Model model, final ImplicitActionDataBase actionData) {
        final ImplicitActionDataForComplexProperty complexActionData = (ImplicitActionDataForComplexProperty) actionData;

        final PropertySetter nestedBean = new PropertySetter(beanDescriptionCache,
                        complexActionData.getNestedComplexProperty());
        nestedBean.setContext(context);

        // have the nested element point to its parent if possible
        if (nestedBean.computeAggregationType(PARENT_PROPPERTY_KEY) == AggregationType.AS_COMPLEX_PROPERTY) {
            nestedBean.setComplexProperty(PARENT_PROPPERTY_KEY, actionData.parentBean.getObj());
        }

        // start the nested complex property if it implements LifeCycle and is not
        // marked with a @NoAutoStart annotation
        final Object nestedComplexProperty = complexActionData.getNestedComplexProperty();
        if (nestedComplexProperty instanceof LifeCycle
                        && NoAutoStartUtil.notMarkedWithNoAutoStart(nestedComplexProperty)) {
            ((LifeCycle) nestedComplexProperty).start();
        }

        final Object o = intercon.peekObject();

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
