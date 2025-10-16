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

public class PropertyEqualsValue extends PropertyEvaluatorBase {


    String key;
    String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


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
