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

import java.util.Objects;

public final class EncoderVersionTag {

    private final KryoEncoder.SerializerType serializerType;
    private final byte streamVersion;

    public EncoderVersionTag(KryoEncoder.SerializerType serializerType, byte streamVersion) {
        this.serializerType = serializerType;
        this.streamVersion = streamVersion;
    }

    public KryoEncoder.SerializerType serializerType() {
        return serializerType;
    }

    public byte streamVersion() {
        return streamVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EncoderVersionTag))
            return false;
        EncoderVersionTag that = (EncoderVersionTag) o;
        return serializerType == that.serializerType && streamVersion == that.streamVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serializerType, streamVersion);
    }

    @Override
    public String toString() {
        return "EncoderVersionTag[serializerType=" + serializerType + ", streamVersion=" + streamVersion + "]";
    }


    static EncoderVersionTag PATTERN_VANILLA = new EncoderVersionTag(null, (byte) 0);
    static EncoderVersionTag PATTERN_V3 = new EncoderVersionTag(null, (byte) 0);
    static EncoderVersionTag PATTERN_V4 = new EncoderVersionTag(null, (byte) 0);
    static EncoderVersionTag PATTERN_V5 = new EncoderVersionTag(null, (byte) 0);

    static EncoderVersionTag VANILLA = new EncoderVersionTag(KryoEncoder.SerializerType.Vanilla, KryoClassicConstants.STREAM_VERSION_0);
    static EncoderVersionTag CUSTOM_V3 = new EncoderVersionTag(KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_3);
    static EncoderVersionTag CUSTOM_V4 = new EncoderVersionTag(KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_4);
    static EncoderVersionTag CUSTOM_V5 = new EncoderVersionTag(KryoEncoder.SerializerType.Custom, KryoClassicConstants.STREAM_VERSION_5);


    static final String CUSTOM_KRYO_SUFFIX_V1 = "-custom-v1.kryo";
    static final String CUSTOM_KRYO_SUFFIX_V2 = "-custom-v2.kryo";
    static final String CUSTOM_KRYO_SUFFIX_V3 = "-custom-v3.kryo";
    static final String CUSTOM_KRYO_SUFFIX_V4 = "-custom-v4.kryo";
    static final String CUSTOM_KRYO_SUFFIX_V5 = "-custom-v5.kryo";
    static final String VANILLA_KRYO_SUFFIX = "-vanilla.kryo";


    static final String DIRECT_LOG_SUFFIX = ".log";
    static final String LOG_SUFFIX_VANILLA = ".log-vanilla";
    static final String LOG_SUFFIX_V3 = ".log-v3";
    static final String LOG_SUFFIX_V4 = ".log-v4";
    static final String LOG_SUFFIX_V5 = ".log-v5";

    public String toSuffix() {

        if (serializerType == null) {
            if (this == PATTERN_VANILLA)
                return LOG_SUFFIX_VANILLA;
            if (this == PATTERN_V3)
                return LOG_SUFFIX_V3;
            if (this == PATTERN_V4)
                return LOG_SUFFIX_V4;
            if (this == PATTERN_V5)
                return LOG_SUFFIX_V5;
        }


        if (serializerType == KryoEncoder.SerializerType.Vanilla) {
            return VANILLA_KRYO_SUFFIX;
        }

        if (serializerType == KryoEncoder.SerializerType.Custom) {
            if (streamVersion == KryoClassicConstants.STREAM_VERSION_3)
                return CUSTOM_KRYO_SUFFIX_V3;
            if (streamVersion == KryoClassicConstants.STREAM_VERSION_4)
                return CUSTOM_KRYO_SUFFIX_V4;
            if (streamVersion == KryoClassicConstants.STREAM_VERSION_5)
                return CUSTOM_KRYO_SUFFIX_V5;
        }

        throw new

                IllegalArgumentException("Unknown EncoderType: " + this);

    }


}
