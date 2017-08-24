package ch.qos.logback.core.net;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HardenedObjectInputStreamTest {

    ByteArrayOutputStream bos;
    ObjectOutputStream oos;
    HardenedObjectInputStream inputStream;
    String[] whitelist = new String[] {Innocent.class.getName()};
    
    @Before
    public void setUp() throws Exception {
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void smoke() throws ClassNotFoundException, IOException {
        Innocent innocent = new Innocent();
        innocent.setAnInt(1);
        innocent.setAnInteger(2);
        innocent.setaString("smoke");
        Innocent back = writeAndRead(innocent);
        assertEquals(innocent, back);
    }



    private Innocent writeAndRead(Innocent innocent) throws IOException, ClassNotFoundException {
        writeObject(oos, innocent);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        inputStream = new HardenedObjectInputStream(bis, whitelist);
        Innocent fooBack = (Innocent) inputStream.readObject();
        inputStream.close();
        return fooBack;
    }
    
    private void writeObject(ObjectOutputStream oos, Object o) throws IOException {
        oos.writeObject(o);
        oos.flush();
        oos.close();
    }
    
}
