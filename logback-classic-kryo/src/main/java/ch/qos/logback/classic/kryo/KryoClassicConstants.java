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
package ch.qos.logback.classic.kryo;

public class KryoClassicConstants {


    public static final int DEFAULT_MAX_EVENTS_BEFORE_RESET = 8*1024;

    /** Magic identifying a Kryo event stream. */
    public static final byte[] STREAM_MAGIC = { 'S', 'T', 'W', 'D' };

    /**
     * Version 0: Vanilla, i.e. Kryo's default serializers. Event payloads are not framed.
     */
    public static final byte STREAM_VERSION_0 = 0;

    /**
     * Version 5: Custom uses {@link LoggingEventKryoVOSerializerV5} (presence bitmap,
     * packed epoch-second/nanos delta, shared long-string serializer). Event payloads
     * are framed ({@link StreamFrameV5}).
     */
    public static final byte STREAM_VERSION_5 = 5;


    /** Size of the fixed stream header: magic (4) + version (1) + flags (1). */
    public static final int STREAM_HEADER_SIZE = 6;

    /** True when the body after the stream header uses framed EVENT/CLOSE records. */
    public static boolean isFramed(byte streamVersion) {
        return streamVersion == STREAM_VERSION_5;
    }

    /** Flag bit: Custom serializers (unset means Vanilla). */
    public static final byte FLAG_CUSTOM_SERIALIZER = 0x01;

}
