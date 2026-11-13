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
module ch.qos.logback.core.kryo {
    // kryo5 is not modular, its Automatic-Module-Name is com.esotericsoftware.kryo.kryo5
    // transitive because kryo types, e.g. Serializer, Kryo, Input, Output, appear in our API
    requires transitive com.esotericsoftware.kryo.kryo5;

    exports ch.qos.logback.core.kryo;
}
