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
package ch.qos.logback.classic;


/**
 * @author ceki
 */
public class HLoggerContext {

    private HLogger root;
    private int size;

    public HLoggerContext() {
        this.root = new HLogger("root", null);
        this.root.setLevel(Level.DEBUG);
        size = 1;
    }

    /**
     * Return this contexts root logger
     *
     * @return
     */
    public HLogger getRootLogger() {
        return root;
    }

    public HLogger getLogger(final String name) {

        int i = 0;
        HLogger HLogger = root;
        HLogger childHLogger = null;
        String childName;

        while (true) {
            int h = name.indexOf('.', i);
            if (h == -1) {
                childName = name.substring(i);
            } else {
                childName = name.substring(i, h);
            }
            // move i left of the last point
            i = h + 1;

            synchronized (HLogger) {
                childHLogger = HLogger.getChildBySuffix(childName);
                if (childHLogger == null) {
                    childHLogger = HLogger.createChildByLastNamePart(childName);
                    incSize();
                }
            }
            HLogger = childHLogger;
            if (h == -1) {
                return childHLogger;
            }
        }
    }

    private synchronized void incSize() {
        size++;
    }

    int size() {
        return size;
    }

    /**
     * Check if the named logger exists in the hierarchy. If so return
     * its reference, otherwise returns <code>null</code>.
     *
     * @param name the name of the logger to search for.
     */
    HLogger exists(String name) {
        int i = 0;
        HLogger HLogger = root;
        HLogger childHLogger = null;
        String childName;
        while (true) {
            int h = name.indexOf('.', i);
            if (h == -1) {
                childName = name.substring(i);
            } else {
                childName = name.substring(i, h);
            }
            // move i left of the last point
            i = h + 1;

            synchronized (HLogger) {
                childHLogger = HLogger.getChildBySuffix(childName);
                if (childHLogger == null) {
                    return null;
                }
            }
            HLogger = childHLogger;
            if (h == -1) {
                if (childHLogger.getName().equals(name)) {
                    return childHLogger;
                } else {
                    return null;
                }
            }
        }
    }
}
