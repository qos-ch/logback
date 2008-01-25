package ch.qos.logback.access.spi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyResponse;

public class AccessEventTest extends TestCase {

  private Object buildSerializedAccessEvent() throws IOException,
      ClassNotFoundException {
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

    assertEquals(DummyResponse.DUMMY_DEFAULT_HDEADER_MAP, aeBack
        .getResponseHeaderMap());
    assertEquals(DummyResponse.DUMMY_DEFAULT_HDEADER_MAP.get("x"), aeBack
        .getResponseHeader("x"));
    assertEquals(DummyResponse.DUMMY_DEFAULT_HDEADER_MAP.get("headerName1"),
        aeBack.getResponseHeader("headerName1"));
    assertEquals(DummyResponse.DUMMY_DEFAULT_HDEADER_MAP.size(), aeBack
        .getResponseHeaderNameList().size());
    assertEquals(DummyResponse.DUMMY_DEFAULT_CONTENT_COUNT, aeBack
        .getContentLength());
    assertEquals(DummyResponse.DUMMY_DEFAULT_STATUS, aeBack.getStatusCode());

  }

}
