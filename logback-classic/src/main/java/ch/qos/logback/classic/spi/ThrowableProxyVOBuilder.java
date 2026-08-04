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
package ch.qos.logback.classic.spi;

/**
 * Builder for {@link ThrowableProxyVO} that instantiates an instance and
 * populates its fields one by one.
 *
 * <p>Use this when assembling a {@link ThrowableProxyVO} from individual
 * values. To copy an existing {@link IThrowableProxy}, prefer
 * {@link ThrowableProxyVO#build(IThrowableProxy)}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 * ThrowableProxyVO vo = ThrowableProxyVO.builder()
 *     .setClassName("java.lang.RuntimeException")
 *     .setMessage("boom")
 *     .setStackTraceElementProxyArray(steps)
 *     .build();
 * </pre>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.2
 * @see ThrowableProxyVO#build(IThrowableProxy)
 */
public class ThrowableProxyVOBuilder {

    private final ThrowableProxyVO throwableProxyVO;

    public ThrowableProxyVOBuilder() {
        this.throwableProxyVO = new ThrowableProxyVO();
    }

    public ThrowableProxyVOBuilder setClassName(String className) {
        throwableProxyVO.setClassName(className);
        return this;
    }

    public ThrowableProxyVOBuilder setMessage(String message) {
        throwableProxyVO.setMessage(message);
        return this;
    }

    public ThrowableProxyVOBuilder setOverridingMessage(String overridingMessage) {
        throwableProxyVO.setOverridingMessage(overridingMessage);
        return this;
    }

    public ThrowableProxyVOBuilder setCommonFramesCount(int commonFramesCount) {
        throwableProxyVO.setCommonFramesCount(commonFramesCount);
        return this;
    }

    public ThrowableProxyVOBuilder setStackTraceElementProxyArray(
            StackTraceElementProxy[] stackTraceElementProxyArray) {
        throwableProxyVO.setStackTraceElementProxyArray(stackTraceElementProxyArray);
        return this;
    }

    public ThrowableProxyVOBuilder setCause(IThrowableProxy cause) {
        throwableProxyVO.setCause(cause);
        return this;
    }

    public ThrowableProxyVOBuilder setSuppressed(IThrowableProxy[] suppressed) {
        throwableProxyVO.setSuppressed(suppressed);
        return this;
    }

    public ThrowableProxyVOBuilder setCyclic(boolean cyclic) {
        throwableProxyVO.setCyclic(cyclic);
        return this;
    }

    /**
     * Returns the constructed {@link ThrowableProxyVO} instance.
     *
     * @return the value object with fields set so far
     */
    public ThrowableProxyVO build() {
        return throwableProxyVO;
    }
}
