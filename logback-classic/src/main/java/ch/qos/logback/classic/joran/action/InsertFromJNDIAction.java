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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.InsertFromJNDIModel;
import ch.qos.logback.core.model.Model;

/**
 * Insert an env-entry found in JNDI as a new context variable
 * 
 * @author Ceki Gulcu
 *
 */
public class InsertFromJNDIAction extends BaseModelAction {

    public static final String ENV_ENTRY_NAME_ATTR = "env-entry-name";
    public static final String AS_ATTR = "as";

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        InsertFromJNDIModel ifjm = new InsertFromJNDIModel();
        ifjm.setEnvEntryName(attributes.getValue(ENV_ENTRY_NAME_ATTR));
        ifjm.setAs(attributes.getValue(AS_ATTR));
        ifjm.setScopeStr(attributes.getValue(SCOPE_ATTRIBUTE));

        return ifjm;
    }

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, seic, name, attributes);
        validator.validateGivenAttribute(ENV_ENTRY_NAME_ATTR);
        validator.validateGivenAttribute(AS_ATTR);

        return validator.isValid();
    }

}
