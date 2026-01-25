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
package ch.qos.logback.core.joran.conditional;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.conditional.ByPropertiesConditionModel;
import org.xml.sax.Attributes;

public class ByPropertiesConditionAction extends BaseModelAction  {


    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
                                      Attributes attributes) {
        ByPropertiesConditionModel sngm = new ByPropertiesConditionModel();
        sngm.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        return sngm;
    }

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, seic, name, attributes);
        validator.validateClassAttribute();
        return validator.isValid();
    }

}


