package ch.qos.logback.core.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOByteBufferedOutputStream extends OutputStream {

    static final int DEFAULT_BYTE_BUFFER_CAPACITY =  64 * 1024;

    final OutputStream os;
    final ByteBuffer byteBuffer;
    final FileChannel fc;

    public NIOByteBufferedOutputStream(OutputStream os, int bufferCapacity) {
        this.os = os;
        if (os instanceof FileOutputStream) {
            FileOutputStream rfos = (FileOutputStream) os;
            fc = rfos.getChannel();
            byteBuffer = ByteBuffer.allocateDirect(bufferCapacity);
            System.out.println("*************FC "+DEFAULT_BYTE_BUFFER_CAPACITY);
        } else {
            fc = null;
            byteBuffer = ByteBuffer.allocate(bufferCapacity);
        }
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
        if (fc == null) {
            os.write(byteBuffer.array(), 0, byteBuffer.limit());
        } else {
            fc.write(byteBuffer);
        }
        byteBuffer.clear();
    }

    public void writeBB(ByteBuffer bb) throws IOException {
        bb.flip();
        fc.write(bb);
        bb.clear();
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
