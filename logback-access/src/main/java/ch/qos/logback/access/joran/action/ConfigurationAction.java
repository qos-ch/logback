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
package ch.qos.logback.access.joran.action;

import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.ContextUtil;

public class ConfigurationAction extends Action {
    static final String INTERNAL_DEBUG_ATTR = "debug";
    static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback-access.debug";

    @Override
    public void begin(InterpretationContext ec, String name, Attributes attributes) {

        // See LBCLASSIC-225 (the system property is looked up first. Thus, it overrides
        // the equivalent property in the config file. This reversal of scope priority is justified
        // by the use case: the admin trying to chase rogue config file
        String debugAttrib = System.getProperty(DEBUG_SYSTEM_PROPERTY_KEY);
        if (debugAttrib == null) {
            debugAttrib = attributes.getValue(INTERNAL_DEBUG_ATTR);
        }

        if (OptionHelper.isEmpty(debugAttrib) || debugAttrib.equals("false") || debugAttrib.equals("null")) {
            addInfo(INTERNAL_DEBUG_ATTR + " attribute not set");
        } else {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(context, new OnConsoleStatusListener());
        }

        new ContextUtil(context).addHostNameAsProperty();

        // the context is appender attachable, so it is pushed on top of the stack
        ec.pushObject(getContext());
    }

    @Override
    public void end(InterpretationContext ec, String name) {
        addInfo("End of configuration.");
        ec.popObject();
    }
}
