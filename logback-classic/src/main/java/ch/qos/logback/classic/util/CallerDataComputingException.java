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
package ch.qos.logback.classic.util;

/**
 * A throwable whose stack trace is caller data: leading frames of this class and of the
 * designated classes are removed, and the remainder is capped at a fixed depth.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class CallerDataComputingException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;


    @Override
    public String getMessage() {
        return "Special throwable used to compute caller data. See stack trace below for caller location.";
    }

    /**
     * @param fqnsToShave class names shaved from the top of the stack, together with this throwable
     * @param maxDepth maximum number of frames retained after shaving
     */
    public CallerDataComputingException(String[] fqnsToShave, int maxDepth) {
        super();
        if (fqnsToShave == null) {
            throw new IllegalArgumentException("fqnsToShave cannot be null");
        }
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1, was " + maxDepth);
        }
        StackTraceElement[] steArray = getStackTrace();
        int start = indexPastShavedPrefix(steArray, fqnsToShave);
        int available = steArray.length - start;
        int depth = Math.min(maxDepth, available);
        StackTraceElement[] trimmed = new StackTraceElement[depth];
        System.arraycopy(steArray, start, trimmed, 0, depth);
        setStackTrace(trimmed);
    }

    /**
     * The first remaining caller frame. Status lines append this text, so they name
     * the caller rather than this class.
     */
    @Override
    public String toString() {
        StackTraceElement[] stack = getStackTrace();
        if (stack.length == 0) {
            return "";
        }
        return stack[0].toString();
    }

    /**
     * Index of the first frame that is neither this throwable nor one of {@code fqnsToShave}.
     * Only the leading run of such frames is shaved. Frames of those classes that appear
     * later are kept.
     */
    private int indexPastShavedPrefix(StackTraceElement[] steArray, String[] fqnsToShave) {
        String ownName = getClass().getName();
        int start = 0;
        while (start < steArray.length) {
            String className = steArray[start].getClassName();
            if (!ownName.equals(className) && !contains(fqnsToShave, className)) {
                break;
            }
            start++;
        }
        return start;
    }

    private static boolean contains(String[] names, String className) {
        for (String name : names) {
            if (name.equals(className)) {
                return true;
            }
        }
        return false;
    }
}
