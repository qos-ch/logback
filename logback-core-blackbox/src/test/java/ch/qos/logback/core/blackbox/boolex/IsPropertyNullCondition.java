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

package ch.qos.logback.core.blackbox.boolex;

import ch.qos.logback.core.boolex.PropertyEvaluatorBase;

public class IsPropertyNullCondition  extends PropertyEvaluatorBase {

    String key;

    public void start() {
        if (key == null) {
            addError("In IsPropertyNullCondition 'key' parameter cannot be null");
            return;
        }
        super.start();
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean evaluate() {
        return isNull(key);
    }
}