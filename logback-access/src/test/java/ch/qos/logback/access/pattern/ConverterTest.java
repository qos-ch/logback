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


import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.model.TimestampModel;
import jakarta.servlet.http.Cookie;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTest {

    AccessEvent event;
    DummyRequest request = new DummyRequest();
    DummyResponse response = new DummyResponse();
    AccessContext accessContext = new AccessContext();

    Locale defaultLocale = Locale.getDefault();

    @BeforeEach
    public void setUp() throws Exception {
        event = createEvent();
    }

    @AfterEach
    public void tearDown() throws Exception {
        event = null;
        request = null;
        response = null;
        Locale.setDefault(defaultLocale);
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

    @Test
    public void testDateConverter_AU_locale() {
        DateConverter converter = new DateConverter();
        List<String> optionsList = Lists.list(CoreConstants.CLF_DATE_PATTERN, "Australia/Sydney", "en-AU");

        converter.setOptionList(optionsList);
        converter.start();
        Instant instant = Instant.parse("2022-10-21T10:30:20.800Z");

        System.out.println(instant.toEpochMilli());

        event.setTimeStamp(instant.toEpochMilli());
        String result = converter.convert(event);
        assertEquals("21/Oct/2022:21:30:20 +1100", result);
        System.out.println(result);

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

    private AccessEvent createEvent() {
        DummyServerAdapter dummyAdapter = new DummyServerAdapter(request, response);
        return new AccessEvent(accessContext, request, response, dummyAdapter);
    }

}
