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

import ch.qos.logback.core.spi.ContextAware;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerAction extends Action {

    boolean inError = false;
    Boolean effectivelyAdded = null;
    StatusListener statusListener = null;

    public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {
        inError = false;
        effectivelyAdded = null;
        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
            addError("Missing class name for statusListener. Near [" + name + "] line " + getLineNumber(ec));
            inError = true;
            return;
        }

        try {
            statusListener = (StatusListener) OptionHelper.instantiateByClassName(className, StatusListener.class, context);
            effectivelyAdded = ec.getContext().getStatusManager().add(statusListener);
            if (statusListener instanceof ContextAware) {
                ((ContextAware) statusListener).setContext(context);
            }
            addInfo("Added status listener of type [" + className + "]");
            ec.pushObject(statusListener);
        } catch (Exception e) {
            inError = true;
            addError("Could not create an StatusListener of type [" + className + "].", e);
            throw new ActionException(e);
        }

    }

    public void finish(InterpretationContext ec) {
    }

    public void end(InterpretationContext ec, String e) {
        if (inError) {
            return;
        }
        if (isEffectivelyAdded() && statusListener instanceof LifeCycle) {
            ((LifeCycle) statusListener).start();
        }
        Object o = ec.peekObject();
        if (o != statusListener) {
            addWarn("The object at the of the stack is not the statusListener pushed earlier.");
        } else {
            ec.popObject();
        }
    }

    private boolean isEffectivelyAdded() {
        if (effectivelyAdded == null)
            return false;
        return effectivelyAdded;
    }
}
