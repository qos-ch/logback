package ch.qos.logback.core.util;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NIOByteBufferedOutputStreamTest {

    
    static String UTF8 = "UTF-8";
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NIOByteBufferedOutputStream nbbos = new NIOByteBufferedOutputStream(baos, 16);
        nbbos.write("hello".getBytes(UTF8));
        nbbos.close();
        verifyOutput("hello", baos.toByteArray());
    }

    
    @Test
    public void writeMoreThanBufferSize() throws IOException {
        String input = "hello world";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NIOByteBufferedOutputStream nbbos = new NIOByteBufferedOutputStream(baos, 4);
        nbbos.write(input.getBytes(UTF8));
        nbbos.close();
        verifyOutput(input, baos.toByteArray());
    }
    
    private void verifyOutput(String witnessStr, byte[] byteArray) throws IOException {
        byte[] witness = witnessStr.getBytes(UTF8);
        if(witness.length != byteArray.length) {
            fail("witness.length of "+witness.length + " differs from byteArray.length of "+ byteArray.length);
        }
        for(int i = 0; i < witness.length; i++) {
            if(witness[i] != byteArray[i]) {
                fail("difference at position "+i);
            }
        }
    }

}
