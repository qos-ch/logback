/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.action;

import java.util.Stack;

import ch.qos.logback.core.joran.spi.ElementPath;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.NoAutoStartUtil;
import ch.qos.logback.core.joran.util.PropertySetter;
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.AggregationType;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

/**
 * This action is responsible for tying together a parent object with a child
 * element for which there is no explicit rule.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class NestedComplexPropertyIA extends ImplicitAction {

    // actionDataStack contains ActionData instances
    // We use a stack of ActionData objects in order to support nested
    // elements which are handled by the same NestedComplexPropertyIA instance.
    // We push a ActionData instance in the isApplicable method (if the
    // action is applicable) and pop it in the end() method.
    // The XML well-formedness property will guarantee that a push will eventually
    // be followed by a corresponding pop.
    Stack<IADataForComplexProperty> actionDataStack = new Stack<IADataForComplexProperty>();

    private final BeanDescriptionCache beanDescriptionCache;

    public NestedComplexPropertyIA(BeanDescriptionCache beanDescriptionCache) {
        this.beanDescriptionCache = beanDescriptionCache;
    }

    public boolean isApplicable(ElementPath elementPath, Attributes attributes, InterpretationContext ic) {

        String nestedElementTagName = elementPath.peekLast();

        // calling ic.peekObject with an empty stack will throw an exception
        if (ic.isEmpty()) {
            return false;
        }

        Object o = ic.peekObject();
        PropertySetter parentBean = new PropertySetter(beanDescriptionCache, o);
        parentBean.setContext(context);

        AggregationType aggregationType = parentBean.computeAggregationType(nestedElementTagName);

        switch (aggregationType) {
        case NOT_FOUND:
        case AS_BASIC_PROPERTY:
        case AS_BASIC_PROPERTY_COLLECTION:
            return false;

            // we only push action data if NestComponentIA is applicable
        case AS_COMPLEX_PROPERTY_COLLECTION:
        case AS_COMPLEX_PROPERTY:
            IADataForComplexProperty ad = new IADataForComplexProperty(parentBean, aggregationType, nestedElementTagName);
            actionDataStack.push(ad);

            return true;
        default:
            addError("PropertySetter.computeAggregationType returned " + aggregationType);
            return false;
        }
    }

    public void begin(InterpretationContext ec, String localName, Attributes attributes) {
        // LogLog.debug("in NestComponentIA begin method");
        // get the action data object pushed in isApplicable() method call
        IADataForComplexProperty actionData = (IADataForComplexProperty) actionDataStack.peek();

        String className = attributes.getValue(CLASS_ATTRIBUTE);
        // perform variable name substitution
        className = ec.subst(className);

        Class<?> componentClass = null;
        try {

            if (!OptionHelper.isEmpty(className)) {
                componentClass = Loader.loadClass(className, context);
            } else {
                // guess class name via implicit rules
                PropertySetter parentBean = actionData.parentBean;
                componentClass = parentBean.getClassNameViaImplicitRules(actionData.getComplexPropertyName(), actionData.getAggregationType(),
                                ec.getDefaultNestedComponentRegistry());
            }

            if (componentClass == null) {
                actionData.inError = true;
                String errMsg = "Could not find an appropriate class for property [" + localName + "]";
                addError(errMsg);
                return;
            }

            if (OptionHelper.isEmpty(className)) {
                addInfo("Assuming default type [" + componentClass.getName() + "] for [" + localName + "] property");
            }

            actionData.setNestedComplexProperty(componentClass.getConstructor().newInstance());

            // pass along the repository
            if (actionData.getNestedComplexProperty() instanceof ContextAware) {
                ((ContextAware) actionData.getNestedComplexProperty()).setContext(this.context);
            }
            // addInfo("Pushing component [" + localName
            // + "] on top of the object stack.");
            ec.pushObject(actionData.getNestedComplexProperty());

        } catch (Exception oops) {
            actionData.inError = true;
            String msg = "Could not create component [" + localName + "] of type [" + className + "]";
            addError(msg, oops);
        }

    }

    public void end(InterpretationContext ec, String tagName) {

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

        Object o = ec.peekObject();

        if (o != actionData.getNestedComplexProperty()) {
            addError("The object on the top the of the stack is not the component pushed earlier.");
        } else {
            ec.popObject();
            // Now let us attach the component
            switch (actionData.aggregationType) {
            case AS_COMPLEX_PROPERTY:
                actionData.parentBean.setComplexProperty(tagName, actionData.getNestedComplexProperty());

                break;
            case AS_COMPLEX_PROPERTY_COLLECTION:
                actionData.parentBean.addComplexProperty(tagName, actionData.getNestedComplexProperty());
                break;
            default:
                addError("Unexpected aggregationType " + actionData.aggregationType);
            }
        }
    }

}
