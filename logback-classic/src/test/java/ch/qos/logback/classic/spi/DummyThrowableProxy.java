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
package ch.qos.logback.classic.spi;

public class DummyThrowableProxy implements IThrowableProxy {

    private String className;
    private String message;
    private int commonFramesCount;
    private StackTraceElementProxy[] stackTraceElementProxyArray;
    private IThrowableProxy cause;
    private IThrowableProxy[] suppressed;
    private boolean cyclic;

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public int getCommonFrames() {
        return commonFramesCount;
    }

    public void setCommonFramesCount(final int commonFramesCount) {
        this.commonFramesCount = commonFramesCount;
    }

    @Override
    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return stackTraceElementProxyArray;
    }

    public void setStackTraceElementProxyArray(final StackTraceElementProxy[] stackTraceElementProxyArray) {
        this.stackTraceElementProxyArray = stackTraceElementProxyArray;
    }

    @Override
    public IThrowableProxy getCause() {
        return cause;
    }

    public void setCause(final IThrowableProxy cause) {
        this.cause = cause;
    }

    @Override
    public IThrowableProxy[] getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(final IThrowableProxy[] suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(final boolean cyclic) {
        this.cyclic = cyclic;
    }
}
