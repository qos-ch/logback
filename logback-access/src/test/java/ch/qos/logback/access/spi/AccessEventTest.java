package ch.qos.logback.access.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import ch.qos.logback.access.dummy.DummyAccessEventBuilder;

public class AccessEventTest extends TestCase {

  private Object buildSerializedAccessEvent() throws IOException, ClassNotFoundException{
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    AccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
    // average time for the next method: 5000 nanos
    ae.prepareForDeferredProcessing();
    oos.writeObject(ae);
    oos.flush();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);

    return ois.readObject();
  }

  
  public void testSerialization() throws IOException, ClassNotFoundException {
    Object o = buildSerializedAccessEvent();
    assertNotNull(o);
    AccessEvent aeBack = (AccessEvent) o;

    aeBack.getRequestHeaderNames();
    aeBack.getResponseHeader("x");
    aeBack.getResponseHeaderNameList();
    aeBack.getContentLength();
    aeBack.getStatusCode();
    
    
  }

}
