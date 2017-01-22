/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.spi;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class AccessEventSerializationTest {

    private Object buildSerializedAccessEvent() throws IOException, ClassNotFoundException {
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

        assertEquals(DummyResponse.DUMMY_DEFAULT_HEADER_MAP.size(), aeBack.getResponseHeaderMap().size());
        assertNull(aeBack.getResponseHeader("x"));
        assertEquals(DummyResponse.DUMMY_DEFAULT_HEADER_MAP.get("headerName1")[0], aeBack.getResponseHeader("headerName1"));
        assertEquals(DummyResponse.DUMMY_DEFAULT_HEADER_MAP.get("headerName2")[0], aeBack.getResponseHeader("headerName2"));
        assertEquals(DummyResponse.DUMMY_DEFAULT_HEADER_MAP.size(), aeBack.getResponseHeaderNameList().size());
        assertEquals(DummyResponse.DUMMY_DEFAULT_CONTENT_COUNT, aeBack.getContentLength());
        assertEquals(DummyResponse.DUMMY_DEFAULT_STATUS, aeBack.getStatusCode());

        assertEquals(DummyRequest.DUMMY_CONTENT_STRING, aeBack.getRequestContent());

        assertEquals(DummyRequest.DUMMY_RESPONSE_CONTENT_STRING, aeBack.getResponseContent());

        assertEquals(DummyRequest.DUMMY_DEFAULT_ATTR_MAP.get("testKey"), aeBack.getAttribute("testKey"));
    }

    // Web containers may (and will) recycle requests objects. So we must make sure that after
    // we prepared an event for deferred processing it won't be using data from the original
    // HttpRequest object which may at that time represent another request
    @Test
    public void testAttributesAreNotTakenFromRecycledRequestWhenProcessingDeferred() {

        DummyRequest request = new DummyRequest();
        DummyResponse response = new DummyResponse();
        DummyServerAdapter adapter = new DummyServerAdapter(request, response);

        IAccessEvent event = new AccessEvent(request, response, adapter);

        request.setAttribute("testKey", "ORIGINAL");

        event.prepareForDeferredProcessing();

        request.setAttribute("testKey", "NEW");

        // Event should capture the original value
        assertEquals("ORIGINAL", event.getAttribute("testKey"));
    }
}
