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

public interface Builder<E> {

    // 45 characters message
    String MSG_PREFIX = "aaaaabbbbbcccccdddddaaaaabbbbbcccccdddddaaaa";

    // final String MSG_PREFIX = "a";

    E build(int i);
}
