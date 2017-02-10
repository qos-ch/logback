package ch.qos.logback.core.helpers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AsyncOutputStream extends OutputStream {

    AsynchronousFileChannel fileChannel;
    final ByteBuffer byteBuffer;
    long offset = 0;
    public AsyncOutputStream(File file, int bufferCapacity) throws IOException {
        Path path = file.toPath();
        
        if(!Files.exists(path)){
            Files.createFile(path);
        }
        fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        byteBuffer = ByteBuffer.allocate(1024*1024);
    }

    @Override
    public void write(int b) throws IOException {
        if (byteBuffer.remaining() == 0) {
            drainBufferIntoOutputStream();
        }
        byteBuffer.put((byte) b);
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

    private void drainBufferIntoOutputStream() throws IOException {
        byteBuffer.flip();
        fileChannel.write(byteBuffer, offset);
        offset += byteBuffer.limit();
        byteBuffer.clear();
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
