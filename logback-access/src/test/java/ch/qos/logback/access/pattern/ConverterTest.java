/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.pattern;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import jakarta.servlet.http.Cookie;

public class ConverterTest {

    IAccessEvent event;
    DummyRequest request = new DummyRequest();
    DummyResponse response =  new DummyResponse();
    AccessContext accessContext = new AccessContext();

    @Before
    public void setUp() throws Exception {
        event = createEvent();
    }

    @After
    public void tearDown() throws Exception {
        event = null;
        request = null;
        response = null;
    }

    @Test
    public void testContentLengthConverter() {
        final ContentLengthConverter converter = new ContentLengthConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(Long.toString(event.getServerAdapter().getContentLength()), result);
    }

    @Test
    public void testDateConverter() {
        final DateConverter converter = new DateConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(converter.cachingDateFormatter.format(event.getTimeStamp()), result);
    }

    public void testLineLocalPortConverter() {
        final LocalPortConverter converter = new LocalPortConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(Integer.toString(request.getLocalPort()), result);
    }

    @Test
    public void testRemoteHostConverter() {
        final RemoteHostConverter converter = new RemoteHostConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getRemoteHost(), result);
    }

    @Test
    public void testRemoteIPAddressConverter() {
        final RemoteIPAddressConverter converter = new RemoteIPAddressConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getRemoteAddr(), result);
    }

    @Test
    public void testRemoteUserConverter() {
        final RemoteUserConverter converter = new RemoteUserConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getRemoteUser(), result);
    }

    @Test
    public void testRequestAttributeConverter() {
        final RequestAttributeConverter converter = new RequestAttributeConverter();
        final List<String> optionList = new ArrayList<>();
        optionList.add("testKey");
        converter.setOptionList(optionList);
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getAttribute("testKey"), result);
    }

    @Test
    public void testRequestCookieConverter() {
        final RequestCookieConverter converter = new RequestCookieConverter();
        final List<String> optionList = new ArrayList<>();
        optionList.add("testName");
        converter.setOptionList(optionList);
        converter.start();
        final String result = converter.convert(event);
        final Cookie cookie = request.getCookies()[0];
        assertEquals(cookie.getValue(), result);
    }

    @Test
    public void testRequestHeaderConverter() {
        final RequestHeaderConverter converter = new RequestHeaderConverter();
        final List<String> optionList = new ArrayList<>();
        optionList.add("headerName1");
        converter.setOptionList(optionList);
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getHeader("headerName1"), result);
    }

    @Test
    public void testRequestMethodConverter() {
        final RequestMethodConverter converter = new RequestMethodConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getMethod(), result);
    }

    @Test
    public void testRequestProtocolConverter() {
        final RequestProtocolConverter converter = new RequestProtocolConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getProtocol(), result);
    }

    @Test
    public void testRequestURIConverter() {
        final RequestURIConverter converter = new RequestURIConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getRequestURI(), result);
    }

    @Test
    public void testRequestURLConverter() {
        final RequestURLConverter converter = new RequestURLConverter();
        converter.start();
        final String result = converter.convert(event);
        final String expected = request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol();
        assertEquals(expected, result);
    }

    @Test
    public void testResponseHeaderConverter() {
        final ResponseHeaderConverter converter = new ResponseHeaderConverter();
        final List<String> optionList = new ArrayList<>();
        optionList.add("headerName1");
        converter.setOptionList(optionList);
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getHeader("headerName1"), result);
    }

    @Test
    public void testServerNameConverter() {
        final ServerNameConverter converter = new ServerNameConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(request.getServerName(), result);
    }

    @Test
    public void testStatusCodeConverter() {
        final StatusCodeConverter converter = new StatusCodeConverter();
        converter.start();
        final String result = converter.convert(event);
        assertEquals(Integer.toString(event.getServerAdapter().getStatusCode()), result);
    }

    private IAccessEvent createEvent() {
        final DummyServerAdapter dummyAdapter = new DummyServerAdapter(request, response);
        return new AccessEvent(accessContext, request, response, dummyAdapter);
    }

}
