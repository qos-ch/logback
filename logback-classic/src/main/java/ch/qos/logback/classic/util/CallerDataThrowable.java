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
 * A throwable whose stack trace is caller data: leading frames of this class and of a
 * designated class are removed, and the remainder is capped at a fixed depth.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class CallerDataThrowable extends Throwable {

    private static final long serialVersionUID = 1L;

    /**
     * @param fqnToShave class name shaved from the top of the stack, together with this throwable
     * @param maxDepth maximum number of frames retained after shaving
     */
    public CallerDataThrowable(String fqnToShave, int maxDepth) {
        super();
        if (fqnToShave == null) {
            throw new IllegalArgumentException("fqnToShave cannot be null");
        }
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be at least 1, was " + maxDepth);
        }
        StackTraceElement[] steArray = getStackTrace();
        int start = indexPastShavedPrefix(steArray, fqnToShave);
        int available = steArray.length - start;
        int depth = Math.min(maxDepth, available);
        StackTraceElement[] trimmed = new StackTraceElement[depth];
        System.arraycopy(steArray, start, trimmed, 0, depth);
        setStackTrace(trimmed);
    }

    /**
     * Index of the first frame that is neither this throwable nor {@code fqnToShave},
     * after a prefix of those frames. Frames of those classes that appear later are kept.
     */
    private int indexPastShavedPrefix(StackTraceElement[] steArray, String fqnToShave) {
        String ownName = getClass().getName();
        int start = 0;
        for (int i = 0; i < steArray.length; i++) {
            String className = steArray[i].getClassName();
            if (ownName.equals(className) || fqnToShave.equals(className)) {
                start = i + 1;
            } else if (start > 0) {
                break;
            }
        }
        return start;
    }
}
