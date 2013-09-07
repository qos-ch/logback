/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;

public class AccessEventSerializationTest  {

  private Object buildSerializedAccessEvent() throws IOException,
      ClassNotFoundException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();
    // average time for the next method: 5000 nanos
    ae.prepareForDeferredProcessing();
    oos.writeObject(ae);
    oos.flush();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);

    return ois.readObject();
  }

  @Test
  public void testSerialization() throws IOException, ClassNotFoundException {
    Object o = buildSerializedAccessEvent();
    assertNotNull(o);
    IAccessEvent aeBack = (IAccessEvent) o;


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

    assertEquals(DummyRequest.DUMMY_CONTENT_STRING, aeBack
        .getRequestContent());
    
    assertEquals(DummyRequest.DUMMY_RESPONSE_CONTENT_STRING, aeBack
        .getResponseContent());
    
  }

}
