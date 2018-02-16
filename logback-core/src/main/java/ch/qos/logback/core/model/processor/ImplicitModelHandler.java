package ch.qos.logback.core.model.processor;

import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.IADataForBasicProperty;
import ch.qos.logback.core.joran.action.IADataForComplexProperty;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

public class ImplicitModelHandler extends ModelHandlerBase {

    // actionDataStack contains ActionData instances
    // We use a stack of ActionData objects in order to support nested
    // elements which are handled by the same NestedComplexPropertyIA instance.
    // We push a ActionData instance in the isApplicable method (if the
    // action is applicable) and pop it in the end() method.
    // The XML well-formedness property will guarantee that a push will eventually
    // be followed by a corresponding pop.
    Stack<IADataForComplexProperty> actionDataStack = new Stack<IADataForComplexProperty>();

    private final BeanDescriptionCache beanDescriptionCache;

    public ImplicitModelHandler(Context context, BeanDescriptionCache beanDescriptionCache) {
        super(context);
        this.beanDescriptionCache = beanDescriptionCache;
    }

    @Override
    public void handle(InterpretationContext interpretationContext, Model model) {

        // calling ic.peekObject with an empty stack will throw an exception
        if (interpretationContext.isObjectStackEmpty()) {
            return;
        }
        String nestedElementTagName = model.getTag();

        Object o = interpretationContext.peekObject();
        PropertySetter parentBean = new PropertySetter(beanDescriptionCache, o);
        parentBean.setContext(context);

        AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);

        switch (aggregationType) {
        case NOT_FOUND:
            return;
        case AS_BASIC_PROPERTY:
        case AS_BASIC_PROPERTY_COLLECTION:
            IADataForBasicProperty adBasicProperty = new IADataForBasicProperty(parentBean, aggregationType, nestedElementTagName);
            doBasicProperty(interpretationContext, model, adBasicProperty);
            return;
        // we only push action data if NestComponentIA is applicable
        case AS_COMPLEX_PROPERTY_COLLECTION:
        case AS_COMPLEX_PROPERTY:
            IADataForComplexProperty adComplex = new IADataForComplexProperty(parentBean, aggregationType, nestedElementTagName);
            actionDataStack.push(adComplex);
            doComplex(interpretationContext, model, adComplex);
            return;
        default:
            addError("PropertySetter.computeAggregationType returned " + aggregationType);
            return;
        }

    }

    void doBasicProperty(InterpretationContext interpretationContext, Model model, IADataForBasicProperty actionData) {
        String finalBody = interpretationContext.subst(model.getBodyText());
        // get the action data object pushed in isApplicable() method call
        // IADataForBasicProperty actionData = (IADataForBasicProperty) actionDataStack.peek();
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

    @Override
    public void postHandle(InterpretationContext interpretationContext, Model model) {
        if (model.isComponentModel()) {
            // pop the action data object pushed in isApplicable() method call
            // we assume that each this begin
            IADataForComplexProperty actionData = (IADataForComplexProperty) actionDataStack.pop();

            if (actionData.inError) {
                return;
            }

            
            PropertySetter nestedBean = new PropertySetter(beanDescriptionCache, actionData.getNestedComplexProperty());
            nestedBean.setContext(context);

            // have the nested element point to its parent if possible
            if (nestedBean.computeAggregationType("parent") == AggregationType.AS_COMPLEX_PROPERTY) {
                nestedBean.setComplexProperty("parent", actionData.parentBean.getObj());
            }

            // start the nested complex property if it implements LifeCycle and is not
            // marked with a @NoAutoStart annotation
            Object nestedComplexProperty = actionData.getNestedComplexProperty();
            if (nestedComplexProperty instanceof LifeCycle && NoAutoStartUtil.notMarkedWithNoAutoStart(nestedComplexProperty)) {
                ((LifeCycle) nestedComplexProperty).start();
            }

            Object o = interpretationContext.peekObject();

            if (o != actionData.getNestedComplexProperty()) {
                addError("The object on the top the of the stack is not the component pushed earlier.");
            } else {
                interpretationContext.popObject();
                // Now let us attach the component
                switch (actionData.aggregationType) {
                case AS_COMPLEX_PROPERTY:
                    actionData.parentBean.setComplexProperty(model.getTag(), actionData.getNestedComplexProperty());

                    break;
                case AS_COMPLEX_PROPERTY_COLLECTION:
                    actionData.parentBean.addComplexProperty(model.getTag(), actionData.getNestedComplexProperty());
                    break;
                default:
                    addError("Unexpected aggregationType " + actionData.aggregationType);
                }
            }
        }
    }

    public void doComplex(InterpretationContext interpretationContext, Model model, IADataForComplexProperty actionData) {

        String className = model.getClassName();
        // perform variable name substitution
        className = interpretationContext.subst(className);

        Class<?> componentClass = null;
        try {

            if (!OptionHelper.isEmpty(className)) {
                componentClass = Loader.loadClass(className, context);
            } else {
                // guess class name via implicit rules
                PropertySetter parentBean = actionData.parentBean;
                componentClass = parentBean.getClassNameViaImplicitRules(actionData.getComplexPropertyName(), actionData.getAggregationType(),
                                interpretationContext.getDefaultNestedComponentRegistry());
            }

            if (componentClass == null) {
                actionData.inError = true;
                String errMsg = "Could not find an appropriate class for property [" + model.getTag() + "]";
                addError(errMsg);
                return;
            }

            if (OptionHelper.isEmpty(className)) {
                addInfo("Assuming default type [" + componentClass.getName() + "] for [" + model.getTag() + "] property");
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
            String msg = "Could not create component [" + model.getTag() + "] of type [" + className + "]";
            addError(msg, oops);
        }
    }
}
