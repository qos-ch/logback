package ch.qos.logback.core.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class NIOByteBufferedOutputStream extends OutputStream {

    static final int DEFAULT_BYTE_BUFFER_CAPACITY = 1024 * 1024;

    final OutputStream os;
    final ByteBuffer byteBuffer;

    private NIOByteBufferedOutputStream(OutputStream os, int bufferCapacity) {
        this.os = os;
        byteBuffer = ByteBuffer.allocate(bufferCapacity);
    }

    public NIOByteBufferedOutputStream(OutputStream os) {
        this(os, DEFAULT_BYTE_BUFFER_CAPACITY);
    }

    @Override
    public void write(int b) throws IOException {
        if (byteBuffer.remaining() == 0) {
            drainBufferIntoOutputStream();
        }
        byteBuffer.put((byte) b);
    }

    private void drainBufferIntoOutputStream() throws IOException {
        byteBuffer.flip();
        os.write(byteBuffer.array(), 0, byteBuffer.limit());
        byteBuffer.clear();
    }

    @Override
    public void write(byte[] byteArray, int offset, int len) throws IOException {
        int bytesToWrite = len - offset;
        int i = 0;
        while (i < bytesToWrite) {
            while ((i < bytesToWrite) && byteBuffer.remaining() > 0) {
                byteBuffer.put(byteArray[offset + i]);
                i++;
            }
            if (byteBuffer.remaining() == 0) {
                drainBufferIntoOutputStream();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        drainBufferIntoOutputStream();
        os.flush();
    }

    @Override
    public void close() throws IOException {
        drainBufferIntoOutputStream();
        os.close();
    }
}