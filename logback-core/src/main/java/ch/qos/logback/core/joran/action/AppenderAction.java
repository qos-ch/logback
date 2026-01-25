/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;

public class AppenderAction extends BaseModelAction {

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext ic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, ic, name, attributes);
        validator.validateClassAttribute();
        validator.validateNameAttribute();
        return validator.isValid();
    }

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        AppenderModel appenderModel = new AppenderModel();
        appenderModel.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        appenderModel.setName(attributes.getValue(NAME_ATTRIBUTE));
        return appenderModel;
    }

}
