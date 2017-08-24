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

import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.spi.PropertyDefiner;
import org.xml.sax.Attributes;

/**
 * Instantiate class for define property value. Get future property name and
 * property definer class from attributes. Some property definer properties
 * could be used. After defining put new property to context.
 * 
 * @author Aleksey Didik
 */
public class DefinePropertyAction extends Action {

    String scopeStr;
    Scope scope;
    String propertyName;
    PropertyDefiner definer;
    boolean inError;

    public void begin(InterpretationContext ec, String localName, Attributes attributes) throws ActionException {
        // reset variables
        scopeStr = null;
        scope = null;
        propertyName = null;
        definer = null;
        inError = false;

        // read future property name
        propertyName = attributes.getValue(NAME_ATTRIBUTE);
        scopeStr = attributes.getValue(SCOPE_ATTRIBUTE);

        scope = ActionUtil.stringToScope(scopeStr);
        if (OptionHelper.isEmpty(propertyName)) {
            addError("Missing property name for property definer. Near [" + localName + "] line " + getLineNumber(ec));
            inError = true;
            return;
        }

        // read property definer class name
        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
            addError("Missing class name for property definer. Near [" + localName + "] line " + getLineNumber(ec));
            inError = true;
            return;
        }

        // try to instantiate property definer
        try {
            addInfo("About to instantiate property definer of type [" + className + "]");
            definer = (PropertyDefiner) OptionHelper.instantiateByClassName(className, PropertyDefiner.class, context);
            definer.setContext(context);
            if (definer instanceof LifeCycle) {
                ((LifeCycle) definer).start();
            }
            ec.pushObject(definer);
        } catch (Exception oops) {
            inError = true;
            addError("Could not create an PropertyDefiner of type [" + className + "].", oops);
            throw new ActionException(oops);
        }
    }

    /**
     * Now property definer is initialized by all properties and we can put
     * property value to context
     */
    public void end(InterpretationContext ec, String name) {
        if (inError) {
            return;
        }

        Object o = ec.peekObject();

        if (o != definer) {
            addWarn("The object at the of the stack is not the property definer for property named [" + propertyName + "] pushed earlier.");
        } else {
            addInfo("Popping property definer for property named [" + propertyName + "] from the object stack");
            ec.popObject();
            // let's put defined property and value to context but only if it is
            // not null
            String propertyValue = definer.getPropertyValue();
            if (propertyValue != null) {
                ActionUtil.setProperty(ec, propertyName, propertyValue, scope);
            }
        }
    }
}
