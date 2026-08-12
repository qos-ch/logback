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

package ch.qos.logback.core.util;

/**
 * Deprecated alias for {@link FixedIntervalInvocationGate}.
 * <p>
 * Kept for binary and source compatibility. New code should use
 * {@link FixedIntervalInvocationGate}.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.3.6/1.4.6
 * @deprecated Use {@link FixedIntervalInvocationGate} instead.
 * @see FixedIntervalInvocationGate
 */
@Deprecated
public class SimpleInvocationGate extends FixedIntervalInvocationGate {

    /**
     * Creates a gate with {@link FixedIntervalInvocationGate#DEFAULT_INCREMENT}.
     */
    public SimpleInvocationGate() {
        super();
    }

    /**
     * Creates a gate that allows at most one successful passage per
     * {@code anIncrement} period.
     *
     * @param anIncrement duration between allowed invocations; must not be
     *                    {@code null}
     */
    public SimpleInvocationGate(Duration anIncrement) {
        super(anIncrement);
    }
}
