package ch.qos.logback.classic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoggerSerializationTest {

  LoggerContext lc;
  Logger logger;

  ByteArrayOutputStream bos;
  ObjectOutputStream oos;
  ObjectInputStream inputStream;

  @Before
  public void setUp() throws Exception {
    lc = new LoggerContext();
    lc.setName("testContext");
    logger = lc.getLogger(LoggerSerializationTest.class);
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);
  }

  @After
  public void tearDown() throws Exception {
    lc = null;
    logger = null;
    oos.close();
  }
  
  @Test
  public void serialization() throws IOException, ClassNotFoundException {
    Foo foo = new Foo(logger);
    foo.doFoo();
    Foo fooBack = writeAndRead(foo);
    fooBack.doFoo();
  }

  private Foo writeAndRead(Foo foo) throws IOException,
      ClassNotFoundException {
    oos.writeObject(foo);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    return (Foo) inputStream.readObject();
  }
}
