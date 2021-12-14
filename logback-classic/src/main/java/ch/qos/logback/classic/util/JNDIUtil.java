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

import ch.qos.logback.core.CoreConstants;

/**
 * A simple utility class to create and use a JNDI Context.
 *
 * <b>Given JNDI based attacks, replaced with emtpy code returning null or "" 
 * until better options are found.</b>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class JNDIUtil {

    public static Context getInitialContext() throws NamingException {
        return null; 
        //new InitialContext();
    }

    static int counter = 0;
    
    public static String lookup(Context ctx, String name) {
    	return CoreConstants.EMPTY_STRING;
    	
//    	if (ctx == null) {
//            return null;
//        }
//        try {
//            Object lookup = ctx.lookup(name);
//            return lookup == null ? null : lookup.toString();
//        } catch (NamingException e) {
//            return null;
//        }
    }
}
