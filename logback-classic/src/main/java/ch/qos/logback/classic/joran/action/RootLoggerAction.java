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
package ch.qos.logback.classic.joran.action;

import ch.qos.logback.core.joran.action.PreconditionValidator;
import org.xml.sax.Attributes;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import static ch.qos.logback.core.joran.JoranConstants.NULL;
import static ch.qos.logback.core.joran.JoranConstants.INHERITED;
import static ch.qos.logback.core.spi.ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL;

public class RootLoggerAction extends BaseModelAction {

    Logger root;
    boolean inError = false;

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext interpcont, String name, Attributes attributes) {
        PreconditionValidator pv;
        String levelStr = attributes.getValue(JoranConstants.LEVEL_ATTRIBUTE);
        if(NULL.equalsIgnoreCase(levelStr) || INHERITED.equalsIgnoreCase(levelStr)) {
            addError(ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
            return false;
        }
        return true;
    }
    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        RootLoggerModel rootLoggerModel = new RootLoggerModel();
        String levelStr = attributes.getValue(JoranConstants.LEVEL_ATTRIBUTE);
        rootLoggerModel.setLevel(levelStr);

        return rootLoggerModel;
    }

}
