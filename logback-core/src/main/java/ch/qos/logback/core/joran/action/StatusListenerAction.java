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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.StatusListenerModel;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerAction extends BaseModelAction {

    boolean inError = false;
    Boolean effectivelyAdded = null;
    StatusListener statusListener = null;

    @Override
    protected boolean validPreconditions(InterpretationContext interpretationContext, String name, Attributes attributes) {
        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isNullOrEmpty(className)) {
            addError("Missing class name for statusListener. Near [" + name + "] line " + getLineNumber(interpretationContext));
            return false;
        }
        return true;
    }

    @Override
    protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
        StatusListenerModel statusListenerModel = new StatusListenerModel();
        statusListenerModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        return statusListenerModel;
    }


}
