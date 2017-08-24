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

import java.util.Properties;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.OptionHelper;

public class ActionUtil {

    public enum Scope {
        LOCAL, CONTEXT, SYSTEM
    };

    /**
     * Convert a string into a scope. Scole.LOCAL is returned by default.
     * @param scopeStr
     * @return a scope corresponding to the input string;  Scope.LOCAL by default.
     */
    static public Scope stringToScope(String scopeStr) {
        if (Scope.SYSTEM.toString().equalsIgnoreCase(scopeStr))
            return Scope.SYSTEM;
        if (Scope.CONTEXT.toString().equalsIgnoreCase(scopeStr))
            return Scope.CONTEXT;

        return Scope.LOCAL;
    }

    static public void setProperty(InterpretationContext ic, String key, String value, Scope scope) {
        switch (scope) {
        case LOCAL:
            ic.addSubstitutionProperty(key, value);
            break;
        case CONTEXT:
            ic.getContext().putProperty(key, value);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperty(ic, key, value);
        }
    }

    /**
     * Add all the properties found in the argument named 'props' to an
     * InterpretationContext.
     */
    static public void setProperties(InterpretationContext ic, Properties props, Scope scope) {
        switch (scope) {
        case LOCAL:
            ic.addSubstitutionProperties(props);
            break;
        case CONTEXT:
            ContextUtil cu = new ContextUtil(ic.getContext());
            cu.addProperties(props);
            break;
        case SYSTEM:
            OptionHelper.setSystemProperties(ic, props);
        }
    }

}
