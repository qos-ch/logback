package ch.qos.logback.core.encoder;

public class JsonEncoder<E> extends EncoderBase<E> {
    @Override
    public byte[] headerBytes() {
        return new byte[0];
    }

    @Override
    public byte[] encode(E event) {
        return new byte[0];
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
}
