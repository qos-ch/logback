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
    byte streamVersion = STREAM_VERSION_3;

    int eventsInEpoch;
    int maxEventsBeforeReset = MAX_EVENTS_BEFORE_RESET;

    @Override
    public void start() {
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
     * Stream format version written into the header (1, 2, or 3).
     * For Custom, version selects {@link LoggingEventKryoVOSerializerV1} …
     * {@link LoggingEventKryoVOSerializerV5}. V5 uses compact frames.
     */
    public void setStreamVersion(int streamVersion) {
        this.streamVersion = (byte) streamVersion;
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

        int reserved = framePrefixReserve();
        output.setPosition(reserved);
        kryo.writeObject(output, loggerEventKryoVO);
        output.flush();
        int payloadLen = output.position() - reserved;
        int prefixLen;
        int start;
        if (streamVersion == STREAM_VERSION_5) {
            prefixLen = StreamFrameV5.writeEventPrefix(buffer, reserved, payloadLen);
            start = reserved - prefixLen;
        } else {
            StreamFrame.writeEventPrefix(buffer, 0, payloadLen);
            prefixLen = StreamFrame.FRAME_HEADER_SIZE;
            start = 0;
        }
        int framedLen = prefixLen + payloadLen;
        eventsInEpoch++;
        if (eventsInEpoch < maxEventsBeforeReset) {
            return copyOf(buffer, start, framedLen);
        }
        byte[] close = closeCode();
        byte[] header = headerBytes();
        byte[] out = new byte[framedLen + close.length + header.length];
        System.arraycopy(buffer, start, out, 0, framedLen);
        System.arraycopy(close, 0, out, framedLen, close.length);
        System.arraycopy(header, 0, out, framedLen + close.length, header.length);
        resetEpoch();
        return out;
    }

    private int framePrefixReserve() {
        return streamVersion == STREAM_VERSION_5
                ? StreamFrameV5.MAX_PREFIX
                : StreamFrame.FRAME_HEADER_SIZE;
    }

    private byte[] closeCode() {
        return streamVersion == STREAM_VERSION_5
                ? StreamFrameV5.closeCode()
                : StreamFrame.closeCode();
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
            return closeCode();
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
