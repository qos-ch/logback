package ch.qos.logback.core.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class MemMappedBufferedOutputStream extends OutputStream {

    static long DEFAULT_SIZE = 256 * 1024;

    FileChannel fileChannel;
    MappedByteBuffer mappedByteBuffer;
    long offset;

    public MemMappedBufferedOutputStream(FileChannel fileChannel, long offset) throws IOException {
        this.fileChannel = fileChannel;
        this.offset = offset;
        mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, offset, DEFAULT_SIZE);
    }

    @Override
    public void write(int b) throws IOException {
        if (!mappedByteBuffer.hasRemaining()) {
            writeOut();
        }
        mappedByteBuffer.put((byte) b);
    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        int bytesLeftToWrite = len - off;

        while (bytesLeftToWrite > 0) {
            int possible = Math.min(mappedByteBuffer.remaining(), bytesLeftToWrite);
            mappedByteBuffer.put(b, off, possible);
            off += possible;
            bytesLeftToWrite -= possible;
            if (!mappedByteBuffer.hasRemaining()) {
                writeOut();
            }
        }
    }

    private void writeOut() throws IOException {
        offset += mappedByteBuffer.position();
        //mappedByteBuffer.force();
        unmap(mappedByteBuffer);
        mappedByteBuffer = fileChannel.map(MapMode.READ_WRITE, offset, DEFAULT_SIZE);
    }

    @Override
    public void flush() throws IOException {
        mappedByteBuffer.force();
    }

    @Override
    public void close() throws IOException {
        offset += mappedByteBuffer.position();
        //mappedByteBuffer.force();
        unmap(mappedByteBuffer);
        fileChannel.truncate(offset);
        fileChannel.close();
    }

    private void unmap(MappedByteBuffer mappedByteBuffer) {
//        if (mappedByteBuffer instanceof DirectBuffer) {
//            Cleaner cleaner = ((DirectBuffer) mappedByteBuffer).cleaner();
//            cleaner.clean();
//        }
    }

}
