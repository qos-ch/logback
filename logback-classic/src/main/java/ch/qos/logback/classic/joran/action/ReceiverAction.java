/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.model.ReceiverModel;
import ch.qos.logback.classic.net.SocketReceiver;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.SaxEventInterpretationContext;
import ch.qos.logback.core.model.Model;

/**
 * A Joran {@link Action} for a {@link SocketReceiver} configuration.
 *
 * @author Carl Harris
 * @author Ceki G&uuml;lc&uuml;
 */
public class ReceiverAction extends BaseModelAction {

    @Override
    protected Model buildCurrentModel(SaxEventInterpretationContext interpretationContext, String name,
            Attributes attributes) {
        ReceiverModel rm = new ReceiverModel();
        rm.setClassName(attributes.getValue(CLASS_ATTRIBUTE));
        return rm;
    }

    @Override
    protected boolean validPreconditions(SaxEventInterpretationContext seic, String name, Attributes attributes) {
        PreconditionValidator validator = new PreconditionValidator(this, seic, name, attributes);
        validator.validateClassAttribute();
        return validator.isValid();
    }

}
