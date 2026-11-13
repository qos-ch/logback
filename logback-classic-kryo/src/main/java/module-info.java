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
module ch.qos.logback.classic.kryo {
    // types of these modules, e.g. ILoggingEvent, EncoderBase, Marker, appear in our API
    requires transitive org.slf4j;
    requires transitive ch.qos.logback.core;
    requires transitive ch.qos.logback.classic;
    // implies com.esotericsoftware.kryo.kryo5
    requires transitive ch.qos.logback.core.kryo;

    exports ch.qos.logback.classic.kryo;

    // the vanilla stream format serializes LoggingEventKryoVO via kryo's
    // reflection based FieldSerializer
    opens ch.qos.logback.classic.kryo to com.esotericsoftware.kryo.kryo5;
}
