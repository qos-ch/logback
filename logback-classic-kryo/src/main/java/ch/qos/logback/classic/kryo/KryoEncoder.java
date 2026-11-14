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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;

import static ch.qos.logback.classic.kryo.KryoClassicConstants.*;

public class KryoEncoder extends EncoderBase<ILoggingEvent> {


    public enum SerializerType {
        Vanilla,
        Custom
    };

    static int MAX_BUFFER_SIZE = 1024 * 64;

    Kryo kryo;
    byte[] buffer = new byte[MAX_BUFFER_SIZE];
    Output output = new Output(buffer);

    SerializerType serializerType = SerializerType.Custom;
    byte streamVersion = STREAM_VERSION_5;
    /** True once {@link #setStreamVersion(int)} was called, e.g. from configuration. */
    boolean streamVersionExplicitlySet = false;

    int eventsInEpoch;
    int maxEventsBeforeReset = DEFAULT_MAX_EVENTS_BEFORE_RESET;

    @Override
    public void start() {
        // Vanilla streams are unframed and always carry version 0
        if (serializerType == SerializerType.Vanilla && streamVersion != STREAM_VERSION_0) {
            // the default stream version applies to Custom; warn only about an explicit setting
            if (streamVersionExplicitlySet) {
                addWarn("Stream version " + streamVersion + " is not applicable to the Vanilla serializer type. Using stream version "
                        + STREAM_VERSION_0 + " instead.");
            }
            this.streamVersion = STREAM_VERSION_0;
        }
        this.kryo = makeFreshKryo();
        if(kryo == null) {
            addError("Kryo serializer is null");
            return;
        }
        this.eventsInEpoch = 0;
        super.start();
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    boolean isFramed() {
        return KryoClassicConstants.isFramed(streamVersion);
    }

    public int getMaxEventsBeforeReset() {
        return maxEventsBeforeReset;
    }

    public void setMaxEventsBeforeReset(int maxEventsBeforeReset) {
        if (maxEventsBeforeReset < 1) {
            throw new IllegalArgumentException("maxEventsBeforeReset must be >= 1");
        }
        this.maxEventsBeforeReset = maxEventsBeforeReset;
    }

    Kryo makeFreshKryo() {
        return KryoBuilder.build(serializerType, streamVersion);
    }

    public void setSerializerType(SerializerType serializerType) {
        this.serializerType = serializerType;
    }

    public SerializerType getSerializerType() {
        return serializerType;
    }

    /**
     * Stream format version written into the header: {@link KryoClassicConstants#STREAM_VERSION_0}
     * for Vanilla, {@link KryoClassicConstants#STREAM_VERSION_5} for Custom
     * ({@link LoggingEventKryoVOSerializerV5}, framed with {@link StreamFrameV5}).
     */
    public void setStreamVersion(int streamVersion) {
        this.streamVersion = (byte) streamVersion;
        this.streamVersionExplicitlySet = true;
    }

    public byte getStreamVersion() {
        return streamVersion;
    }

    /**
     * Fixed stream preamble: magic (4) + version (1) + flags (1).
     * Written once at the start of the stream by the appender.
     */
    @Override
    public byte[] headerBytes() {
        byte flags = (serializerType == SerializerType.Custom) ? FLAG_CUSTOM_SERIALIZER : 0;
        return new byte[] {
                STREAM_MAGIC[0], STREAM_MAGIC[1], STREAM_MAGIC[2], STREAM_MAGIC[3],
                streamVersion,
                flags
        };
    }

    @Override
    synchronized public byte[] encode(ILoggingEvent loggingEvent) {
        LoggingEventKryoVO loggerEventKryoVO = new LoggingEventKryoVO(loggingEvent);
        if (!isFramed()) {
            output.setPosition(0);
            kryo.writeObject(output, loggerEventKryoVO);
            output.flush();
            return copyOf(buffer, 0, output.position());
        }

        int reserved = StreamFrameV5.MAX_PREFIX;
        output.setPosition(reserved);
        kryo.writeObject(output, loggerEventKryoVO);
        output.flush();
        int payloadLen = output.position() - reserved;
        int prefixLen = StreamFrameV5.writeEventPrefix(buffer, reserved, payloadLen);
        int start = reserved - prefixLen;
        int framedLen = prefixLen + payloadLen;
        eventsInEpoch++;
        if (eventsInEpoch < maxEventsBeforeReset) {
            return copyOf(buffer, start, framedLen);
        }
        byte[] close = StreamFrameV5.closeCode();
        byte[] header = headerBytes();
        byte[] out = new byte[framedLen + close.length + header.length];
        System.arraycopy(buffer, start, out, 0, framedLen);
        System.arraycopy(close, 0, out, framedLen, close.length);
        System.arraycopy(header, 0, out, framedLen + close.length, header.length);
        resetEpoch();
        return out;
    }

    private static byte[] copyOf(byte[] src, int off, int len) {
        byte[] out = new byte[len];
        System.arraycopy(src, off, out, 0, len);
        return out;
    }

    @Override
    synchronized public byte[] footerBytes() {
        if (isFramed() && eventsInEpoch > 0) {
            resetEpoch();
            return StreamFrameV5.closeCode();
        }
        resetEpoch();
        return new byte[0];
    }

    private void resetEpoch() {
        eventsInEpoch = 0;
        kryo = makeFreshKryo();
        if (kryo == null) {
            addError("Kryo serializer is null");
        }
    }
}
