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
package ch.qos.logback.classic.control;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.CoreConstants;

/**
 * This logger context quite optimized for logger retrieval.
 * 
 * <p>It uses a single loggerMap where the key is the logger name and the value
 * is the logger.
 * 
 * <p>This approach acts a lower limit for what is achievable for low memory
 * usage as well as low creation/retrieval times. However, this simplicity also
 * results in slow effective level evaluation, the most frequently exercised
 * part of the API.
 * 
 * <p>This class is expected to contain correct results, and serve to verify
 * the correctness of a more sophisticated implementation.
 * 
 * @author ceki
 */
public class ControlLoggerContext {

    private ControlLogger root;
    //
    // Hashtable loggerMap = new Hashtable();
    Map<String, ControlLogger> loggerMap = new HashMap<String, ControlLogger>();

    public ControlLoggerContext() {
        this.root = new ControlLogger("root", null);
        this.root.setLevel(Level.DEBUG);
    }

    /**
     * Return this contexts root logger
     * 
     * @return
     */
    public ControlLogger getRootLogger() {
        return root;
    }

    public ControlLogger exists(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name parameter cannot be null");
        }

        synchronized (loggerMap) {
            return (ControlLogger) loggerMap.get(name);
        }
    }

    public final ControlLogger getLogger(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name parameter cannot be null");
        }

        synchronized (loggerMap) {
            ControlLogger cl = (ControlLogger) loggerMap.get(name);
            if (cl != null) {
                return cl;
            }
            ControlLogger parent = this.root;

            int i = 0;
            while (true) {
                i = name.indexOf(CoreConstants.DOT, i);
                if (i == -1) {
                    // System.out.println("FINAL-Creating logger named [" + name + "] with
                    // parent " + parent.getName());
                    cl = new ControlLogger(name, parent);
                    loggerMap.put(name, cl);
                    return cl;
                } else {
                    String parentName = name.substring(0, i);
                    ControlLogger p = (ControlLogger) loggerMap.get(parentName);
                    if (p == null) {
                        // System.out.println("INTERMEDIARY-Creating logger [" + parentName
                        // + "] with parent " + parent.getName());
                        p = new ControlLogger(parentName, parent);
                        loggerMap.put(parentName, p);
                    }
                    parent = p;
                }
                // make i move past the last found dot.
                i++;
            }
        }
    }

    public Map<String, ControlLogger> getLoggerMap() {
        return loggerMap;
    }
}
