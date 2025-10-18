/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.boolex;

/**
 * Checks whether a named property is defined in the
 * context (e.g. system properties, environment, or the configured
 * property map used by the surrounding framework).
 *
 * <p>This condition expects a property name to be provided via
 * {@link #setKey(String)}. When {@link #evaluate()} is called it returns
 * {@code true} if the named property is defined and {@code false}
 * otherwise.
 */
public class IsPropertyDefinedCondition extends PropertyConditionBase {

    /**
     * The property name to check for definition. Must be set before
     * starting this evaluator.
     */
    String key;

    /**
     * Start the evaluator. If the required {@link #key} is not set an
     * error is reported and startup is aborted.
     */
    public void start() {
        if (key == null) {
            addError("In IsPropertyDefinedEvaluator 'key' parameter cannot be null");
            return;
        }
        super.start();
    }

    /**
     * Return the configured property name (key) that this evaluator will
     * test for definition.
     *
     * @return the property key, or {@code null} if not set
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the property name (key) to be checked by this evaluator.
     *
     * @param key the property name to check; must not be {@code null}
     */
    public void setKey(String key) {
        this.key = key;
    }


    /**
     * Evaluate whether the configured property is defined.
     *
     * @return {@code true} if the property named by {@link #key} is
     *         defined, {@code false} otherwise
     */
    @Override
    public boolean evaluate() {
        return isDefined(key);
    }
}
