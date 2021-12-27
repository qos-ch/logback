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

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MockInitialContext extends InitialContext {

    public Map<String, Object> map = new HashMap<String, Object>();

    public MockInitialContext() throws NamingException {
        super();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        if (name == null) {
            return null;
        }

        return map.get(name);
    }

}
