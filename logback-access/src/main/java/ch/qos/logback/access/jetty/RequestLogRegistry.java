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
package ch.qos.logback.access.jetty;

import java.util.HashMap;
import java.util.Map;

// this class is currently not used
public class RequestLogRegistry {

    private static Map<String, RequestLogImpl> requestLogRegistry = new HashMap<String, RequestLogImpl>();

    public static void register(RequestLogImpl requestLogImpl) {
        requestLogRegistry.put(requestLogImpl.getName(), requestLogImpl);
    }

    public static RequestLogImpl get(String key) {
        return requestLogRegistry.get(key);
    }

}
