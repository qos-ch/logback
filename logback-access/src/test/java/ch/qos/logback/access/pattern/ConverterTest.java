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

import javax.servlet.http.Cookie;

import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;

public class ConverterTest {

    IAccessEvent event;
    DummyRequest request;
    DummyResponse response;

    @Before
    public void setUp() throws Exception {
        request = new DummyRequest();
        response = new DummyResponse();
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
        ContentLengthConverter converter = new ContentLengthConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(Long.toString(event.getServerAdapter().getContentLength()), result);
    }

    @Test
    public void testDateConverter() {
        DateConverter converter = new DateConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(converter.cachingDateFormatter.format(event.getTimeStamp()), result);
    }

    public void testLineLocalPortConverter() {
        LocalPortConverter converter = new LocalPortConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(Integer.toString(request.getLocalPort()), result);
    }

    @Test
    public void testRemoteHostConverter() {
        RemoteHostConverter converter = new RemoteHostConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getRemoteHost(), result);
    }

    @Test
    public void testRemoteIPAddressConverter() {
        RemoteIPAddressConverter converter = new RemoteIPAddressConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getRemoteAddr(), result);
    }

    @Test
    public void testRemoteUserConverter() {
        RemoteUserConverter converter = new RemoteUserConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getRemoteUser(), result);
    }

    @Test
    public void testRequestAttributeConverter() {
        RequestAttributeConverter converter = new RequestAttributeConverter();
        List<String> optionList = new ArrayList<String>();
        optionList.add("testKey");
        converter.setOptionList(optionList);
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getAttribute("testKey"), result);
    }

    @Test
    public void testRequestCookieConverter() {
        RequestCookieConverter converter = new RequestCookieConverter();
        List<String> optionList = new ArrayList<String>();
        optionList.add("testName");
        converter.setOptionList(optionList);
        converter.start();
        String result = converter.convert(event);
        Cookie cookie = request.getCookies()[0];
        assertEquals(cookie.getValue(), result);
    }

    @Test
    public void testRequestHeaderConverter() {
        RequestHeaderConverter converter = new RequestHeaderConverter();
        List<String> optionList = new ArrayList<String>();
        optionList.add("headerName1");
        converter.setOptionList(optionList);
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getHeader("headerName1"), result);
    }

    @Test
    public void testRequestMethodConverter() {
        RequestMethodConverter converter = new RequestMethodConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getMethod(), result);
    }

    @Test
    public void testRequestProtocolConverter() {
        RequestProtocolConverter converter = new RequestProtocolConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getProtocol(), result);
    }

    @Test
    public void testRequestURIConverter() {
        RequestURIConverter converter = new RequestURIConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getRequestURI(), result);
    }

    @Test
    public void testRequestURLConverter() {
        RequestURLConverter converter = new RequestURLConverter();
        converter.start();
        String result = converter.convert(event);
        String expected = request.getMethod() + " " + request.getRequestURI() + " " + request.getProtocol();
        assertEquals(expected, result);
    }

    @Test
    public void testResponseHeaderConverter() {
        ResponseHeaderConverter converter = new ResponseHeaderConverter();
        List<String> optionList = new ArrayList<String>();
        optionList.add("headerName1");
        converter.setOptionList(optionList);
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getHeader("headerName1"), result);
    }

    @Test
    public void testServerNameConverter() {
        ServerNameConverter converter = new ServerNameConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(request.getServerName(), result);
    }

    @Test
    public void testStatusCodeConverter() {
        StatusCodeConverter converter = new StatusCodeConverter();
        converter.start();
        String result = converter.convert(event);
        assertEquals(Integer.toString(event.getServerAdapter().getStatusCode()), result);
    }

    private IAccessEvent createEvent() {
        DummyServerAdapter dummyAdapter = new DummyServerAdapter(request, response);
        return new AccessEvent(request, response, dummyAdapter);
    }

}
