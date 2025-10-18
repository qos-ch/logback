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
package ch.qos.logback.core.joran.conditional;

/**
 * <p>A condition evaluated during Joran conditional processing.</p>
 *
 * <p>Implementations of this interface encapsulate a boolean test that
 * determines whether a conditional block in a Joran configuration should
 * be processed.</p>
 *
 * <p>Typical implementations evaluate configuration state, environment
 * variables, or other runtime properties.</p>
 *
 * @since 0.9.20
 * @author Ceki G&uuml;lc&uuml;
 */
public interface Condition {

    /**
     * Evaluate the condition.
     *
     * @return {@code true} if the condition is satisfied and the associated
     *         conditional block should be activated; {@code false} otherwise
     */
    boolean evaluate();
}
