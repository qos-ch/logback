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
    String[] whitelist = new String[] { Innocent.class.getName() };

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

//    @Ignore
//    @Test
//    public void denialOfService() throws ClassNotFoundException, IOException {
//        ByteArrayInputStream bis = new ByteArrayInputStream(payload());
//        inputStream = new HardenedObjectInputStream(bis, whitelist);
//        try {
//            Set set = (Set) inputStream.readObject();
//            assertNotNull(set);
//        } finally {
//            inputStream.close();
//        }
//    }
//
//    private byte[] payload() throws IOException {
//        Set root = buildEvilHashset();
//        return serialize(root);
//    }
//
//    private Set buildEvilHashset() {
//        Set root = new HashSet();
//        Set s1 = root;
//        Set s2 = new HashSet();
//        for (int i = 0; i < 100; i++) {
//            Set t1 = new HashSet();
//            Set t2 = new HashSet();
//            t1.add("foo"); // make it not equal to t2
//            s1.add(t1);
//            s1.add(t2);
//            s2.add(t1);
//            s2.add(t2);
//            s1 = t1;
//            s2 = t2;
//        }
//        return root;
//    }
}
