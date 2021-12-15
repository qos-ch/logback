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
package ch.qos.logback.classic.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.core.util.OptionHelper;

/**
 * A simple utility class to create and use a JNDI Context.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class JNDIUtil {

    public static Context getInitialContext() throws NamingException {
        return new InitialContext();
    }

    public static String lookup(Context ctx, String name) throws NamingException {
        if (ctx == null) {
            return null;
        }

        if (OptionHelper.isEmpty(name)) {
            return null;
        }

        if (!name.startsWith(ClassicConstants.JNDI_JAVA_NAMESPACE)) {
            throw new NamingException("JNDI name must start with " + ClassicConstants.JNDI_JAVA_NAMESPACE);
        }

        Object lookup = ctx.lookup(name);
        return lookup == null ? null : lookup.toString();
    }
}
