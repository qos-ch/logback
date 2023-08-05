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
package ch.qos.logback.core.joran.spi;

import java.util.Map;
import java.util.Stack;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;

/**
 * 
 * An InterpretationContext contains the contextual state of a Joran parsing
 * session. {@link Action} objects depend on this context to exchange and store
 * information.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SaxEventInterpretationContext extends ContextAwareBase implements PropertyContainer {
    Stack<Model> modelStack;

    SaxEventInterpreter saxEventInterpreter;

    public SaxEventInterpretationContext(Context context, SaxEventInterpreter saxEventInterpreter) {
        this.context = context;
        this.saxEventInterpreter = saxEventInterpreter;
        this.modelStack = new Stack<>();
    }

    public SaxEventInterpreter getSaxEventInterpreter() {
        return saxEventInterpreter;
    }

    /**
     * Return the Model at the top of the model stack, may return null.
     * 
     * @return
     */
    public Model peekModel() {
        if(modelStack.isEmpty()) {
            return null;
        }
        return modelStack.peek();
    }

    public void pushModel(Model m) {
        modelStack.push(m);
    }

    public boolean isModelStackEmpty() {
        return modelStack.isEmpty();
    }

    public Model popModel() {
        return modelStack.pop();
    }

    public Stack<Model> getCopyOfModelStack() {
        Stack<Model> copy = new Stack<>();
        copy.addAll(modelStack);
        return copy;
    }

    /**
     * If a key is found in propertiesMap then return it. Otherwise, delegate to the
     * context.
     */
    public String getProperty(String key) {
        return context.getProperty(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return null;
    }

    public String subst(String value) {
        if (value == null) {
            return null;
        }

        try {
            return OptionHelper.substVars(value, this, context);
        } catch (ScanException | IllegalArgumentException e) {
            addError("Problem while parsing [" + value + "]", e);
            return value;
        }
    }

}
