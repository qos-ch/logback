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

    public IThrowableProxy getCause() {
        return cause;
    }

    public void setCause(IThrowableProxy cause) {
        this.cause = cause;
    }

    public IThrowableProxy[] getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(IThrowableProxy[] suppressed) {
        this.suppressed = suppressed;
    }
    
    public boolean isCyclic() {
		return cyclic;
	}

	public void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}
}
