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
package ch.qos.logback.classic.net.testObjectBuilders;

import java.io.Serializable;

public class MinimalSerBuilder implements Builder {

    public Object build(int i) {
        return new MinimalSer(i);
    }

}

class MinimalSer implements Serializable {

    private static final long serialVersionUID = 2807646397580899815L;

    String message;

    public MinimalSer(int i) {
        message = Builder.MSG_PREFIX;
    }
}