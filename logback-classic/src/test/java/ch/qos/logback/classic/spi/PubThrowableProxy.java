/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Arrays;

/**
 * Used in tests to represent a ThrowableProxy in a JSON-friendly way.
 */
public class PubThrowableProxy implements IThrowableProxy {

    private String className;
    private String message;
    private int commonFramesCount;

    @JsonAlias("stepArray")
    private StackTraceElementProxy[] stackTraceElementProxyArray;
    private PubThrowableProxy cause;
    private PubThrowableProxy[] suppressed;
    private boolean cyclic;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCommonFrames() {
        return commonFramesCount;
    }

    public void setCommonFramesCount(int commonFramesCount) {
        this.commonFramesCount = commonFramesCount;
    }

    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return stackTraceElementProxyArray;
    }

    public void setStackTraceElementProxyArray(StackTraceElementProxy[] stackTraceElementProxyArray) {
        this.stackTraceElementProxyArray = stackTraceElementProxyArray;
    }

    public PubThrowableProxy getCause() {
        return cause;
    }

    public void setCause(PubThrowableProxy cause) {
        this.cause = cause;
    }

    public PubThrowableProxy[] getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(PubThrowableProxy[] suppressed) {
        this.suppressed = suppressed;
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    @Override
    public String toString() {
        return "PubThrowableProxy{" + "className='" + className + '\'' + ", message='" + message + '\''
                + ", commonFramesCount=" + commonFramesCount + ", stackTraceElementProxyArray=" + Arrays.toString(
                stackTraceElementProxyArray) + ", cause=" + cause + ", suppressed=" + Arrays.toString(suppressed)
                + ", cyclic=" + cyclic + '}';
    }
}
