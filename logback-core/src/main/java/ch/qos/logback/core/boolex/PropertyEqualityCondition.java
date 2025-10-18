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
 * Condition that evaluates to {@code true} when a property
 * equals a specified expected value.
 *
 * <p>The property named by {@link #key} is resolved using the
 * inherited property lookup mechanism (see {@code PropertyConditionBase}).
 * If the resolved property value equals {@link #value} (using
 * {@link String#equals(Object)}), this condition evaluates to {@code true}.
 *
 * @since 1.5.20
 */
public class PropertyEqualityCondition extends PropertyConditionBase {

    /**
     * The property name (key) to look up. Must be set before starting.
     */
    String key;

    /**
     * The expected value to compare the resolved property against.
     */
    String value;

    /**
     * Start the component and validate required parameters.
     * If either {@link #key} or {@link #value} is {@code null}, an error
     * is reported and the component does not start.
     */
    public void start() {
        if (key == null) {
            addError("In PropertyEqualsValue 'key' parameter cannot be null");
            return;
        }
        if (value == null) {
            addError("In PropertyEqualsValue 'value' parameter cannot be null");
            return;
        }
        super.start();
    }

    /**
     * Return the configured expected value.
     *
     * @return the expected value, or {@code null} if not set
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the expected value that the resolved property must equal for
     * this condition to evaluate to {@code true}.
     *
     * @param value the expected value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Return the property key that will be looked up when evaluating the
     * condition.
     *
     * @return the property key, or {@code null} if not set
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the property key to resolve during evaluation.
     *
     * @param key the property key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Evaluate the condition: resolve the property named by {@link #key}
     * and compare it to {@link #value}.
     *
     * @return {@code true} if the resolved property equals the expected
     *         value; {@code false} otherwise
     */
    @Override
    public boolean evaluate() {
        if (key == null) {
            addError("key cannot be null");
            return false;
        }

        String val = p(key);
        if (val == null)
            return false;
        else {
            return val.equals(value);
        }
    }

}
