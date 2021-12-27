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
package ch.qos.logback.core.testUtil;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class MockInitialContextFactory implements InitialContextFactory {
    static MockInitialContext mic;

    static {
        System.out.println("MockInitialContextFactory static called");
        initialize();
    }

    public static void initialize() {
        try {
            mic = new MockInitialContext();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return mic;
    }

    public static MockInitialContext getContext() {
        return mic;
    }

}
