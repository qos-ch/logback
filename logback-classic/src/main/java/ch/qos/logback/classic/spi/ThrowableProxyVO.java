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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class ThrowableProxyVO implements IThrowableProxy, Serializable {

    private static final long serialVersionUID = -773438177285807139L;

    private String className;
    private String message;
    private int commonFramesCount;
    private StackTraceElementProxy[] stackTraceElementProxyArray;
    private IThrowableProxy cause;
    private IThrowableProxy[] suppressed;
    private boolean cyclic;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public int getCommonFrames() {
        return commonFramesCount;
    }

    @Override
    public IThrowableProxy getCause() {
        return cause;
    }

    @Override
    public StackTraceElementProxy[] getStackTraceElementProxyArray() {
        return stackTraceElementProxyArray;
    }

    @Override
    public IThrowableProxy[] getSuppressed() {
        return suppressed;
    }

    @Override
    public boolean isCyclic() {
        return cyclic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + (className == null ? 0 : className.hashCode());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ThrowableProxyVO other = (ThrowableProxyVO) obj;

        if (!Objects.equals(className, other.className) || !Arrays.equals(stackTraceElementProxyArray, other.stackTraceElementProxyArray)
                        || !Arrays.equals(suppressed, other.suppressed) || !Objects.equals(cause, other.cause)) {
            return false;
        }

        return true;
    }

    public static ThrowableProxyVO build(final IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return null;
        }
        final ThrowableProxyVO tpvo = new ThrowableProxyVO();
        tpvo.className = throwableProxy.getClassName();
        tpvo.message = throwableProxy.getMessage();
        tpvo.commonFramesCount = throwableProxy.getCommonFrames();
        tpvo.stackTraceElementProxyArray = throwableProxy.getStackTraceElementProxyArray();
        tpvo.cyclic = throwableProxy.isCyclic();

        final IThrowableProxy cause = throwableProxy.getCause();
        if (cause != null) {
            tpvo.cause = ThrowableProxyVO.build(cause);
        }
        final IThrowableProxy[] suppressed = throwableProxy.getSuppressed();
        if (suppressed != null) {
            tpvo.suppressed = new IThrowableProxy[suppressed.length];
            for (int i = 0; i < suppressed.length; i++) {
                tpvo.suppressed[i] = ThrowableProxyVO.build(suppressed[i]);
            }
        }

        return tpvo;
    }
}
