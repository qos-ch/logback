package ch.qos.logback.core.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DirectNIOByteBufferedOutputStream extends OutputStream {

    static final int DEFAULT_BYTE_BUFFER_CAPACITY = 256 * 1024;

    final FileChannel fileChannel;
    final ByteBuffer byteBuffer;

    private DirectNIOByteBufferedOutputStream(FileChannel fileChannel, int bufferCapacity) {
        this.fileChannel = fileChannel;
        byteBuffer = ByteBuffer.allocateDirect(bufferCapacity);
    }

    public DirectNIOByteBufferedOutputStream(FileChannel fileChannel) {
        this(fileChannel, DEFAULT_BYTE_BUFFER_CAPACITY);
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
        fileChannel.write(byteBuffer);
        byteBuffer.clear();
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        write(byteArray, 0, byteArray.length);
    }
    
    @Override
    public void write(byte[] byteArray, int offset, int len) throws IOException {
        int bytesLeftToWrite = len - offset;
        while (bytesLeftToWrite > 0) {
            int possible = Math.min(byteBuffer.remaining(), bytesLeftToWrite);
            byteBuffer.put(byteArray, offset, possible);
            offset += possible;
            bytesLeftToWrite -= possible;
            if (!byteBuffer.hasRemaining()) {
                drainBufferIntoOutputStream();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        drainBufferIntoOutputStream();
        fileChannel.force(true);
    }

    @Override
    public void close() throws IOException {
        drainBufferIntoOutputStream();
        fileChannel.close();
    }
}