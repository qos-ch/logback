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

import javax.naming.Context;
import javax.naming.NamingException;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.JNDIUtil;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Insert an env-entry found in JNDI as a new context variable  

 * @author Ceki Gulcu
 *
 */
public class InsertFromJNDIAction extends Action {

    public static final String ENV_ENTRY_NAME_ATTR = "env-entry-name";
    public static final String AS_ATTR = "as";

    public void begin(InterpretationContext ec, String name, Attributes attributes) {

        int errorCount = 0;
        String envEntryName = ec.subst(attributes.getValue(ENV_ENTRY_NAME_ATTR));
        String asKey = ec.subst(attributes.getValue(AS_ATTR));

        String scopeStr = attributes.getValue(SCOPE_ATTRIBUTE);
        Scope scope = ActionUtil.stringToScope(scopeStr);

        String envEntryValue;

        if (OptionHelper.isNullOrEmpty(envEntryName)) {
            String lineColStr = getLineColStr(ec);
            addError("[" + ENV_ENTRY_NAME_ATTR + "] missing, around " + lineColStr);
            errorCount++;
        }

        if (OptionHelper.isNullOrEmpty(asKey)) {
            String lineColStr = getLineColStr(ec);
            addError("[" + AS_ATTR + "] missing, around " + lineColStr);
            errorCount++;
        }

        if (errorCount != 0) {
            return;
        }

        try {
            Context ctx = JNDIUtil.getInitialContext();
            envEntryValue = JNDIUtil.lookupString(ctx, envEntryName);
            if (OptionHelper.isNullOrEmpty(envEntryValue)) {
                addError("[" + envEntryName + "] has null or empty value");
            } else {
                addInfo("Setting variable [" + asKey + "] to [" + envEntryValue + "] in [" + scope + "] scope");
                ActionUtil.setProperty(ec, asKey, envEntryValue, scope);
            }
        } catch (NamingException e) {
            addError("Failed to lookup JNDI env-entry [" + envEntryName + "]");
        }

    }

    public void end(InterpretationContext ec, String name) {
    }
}
