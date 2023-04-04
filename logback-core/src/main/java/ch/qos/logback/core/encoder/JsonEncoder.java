package ch.qos.logback.core.encoder;

import ch.qos.logback.core.util.DirectJson;

import java.nio.charset.StandardCharsets;

/**
 * This class allows for concrete encoders to write json log messages.
 *
 * @param <E>
 * @author Henry John Kupty
 */
public abstract class JsonEncoder<E> extends EncoderBase<E> {
    @FunctionalInterface
    protected interface Emitter<E> {
        void write(E event);
    }

    private static final byte[] LINE_BREAK = System.getProperty("line.separator").getBytes(StandardCharsets.UTF_8);

    protected DirectJson jsonWriter = new DirectJson();

    @Override
    public byte[] headerBytes() {
        return LINE_BREAK;
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
}
