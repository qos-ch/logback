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

package ch.qos.logback.core.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.MockInitialContextFactory;

import static org.junit.jupiter.api.Assertions.fail;

public class JNDIUtilTest {

    @Test
    public void ensureJavaNameSpace() throws NamingException {

        try {
            Context ctxt = JNDIUtil.getInitialContext();
            JNDIUtil.lookupString(ctxt, "ldap:...");
        } catch (NamingException e) {
            String excaptionMsg = e.getMessage();
            if (excaptionMsg.startsWith(JNDIUtil.RESTRICTION_MSG))
                return;
            else {
                fail("unexpected exception " + e);
            }
        }

        fail("Should aNot yet implemented");
    }

    @Test
    public void testToStringCast() throws NamingException {
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(CoreTestConstants.JAVA_NAMING_FACTORY_INITIAL, MockInitialContextFactory.class.getCanonicalName());
        Context ctxt = JNDIUtil.getInitialContext(props);
        String x = JNDIUtil.lookupString(ctxt, "java:comp:/inexistent");
        Assertions.assertNull(x);
    }

    public String castToString(Object input) {
        String a = (String) input;
        return a;
    }

}
