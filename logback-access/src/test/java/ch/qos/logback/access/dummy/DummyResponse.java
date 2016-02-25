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
package ch.qos.logback.access.dummy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class DummyResponse implements HttpServletResponse {

    public static final int DUMMY_DEFAULT_STATUS = 200;
    public static final int DUMMY_DEFAULT_CONTENT_COUNT = 1000;
    public static final Map<String, String> DUMMY_DEFAULT_HDEADER_MAP = new HashMap<String, String>();;

    static {
        DUMMY_DEFAULT_HDEADER_MAP.put("headerName1", "headerValue1");
        DUMMY_DEFAULT_HDEADER_MAP.put("headerName2", "headerValue2");
    }

    int status = DUMMY_DEFAULT_STATUS;
    public Map<String, String> headerMap;

    String characterEncoding = null;
    ServletOutputStream outputStream = null;

    public DummyResponse() {
        headerMap = DUMMY_DEFAULT_HDEADER_MAP;
    }

    public void addCookie(Cookie arg0) {
    }

    public void addDateHeader(String arg0, long arg1) {
    }

    public void addHeader(String arg0, String arg1) {
    }

    public void addIntHeader(String arg0, int arg1) {
    }

    public boolean containsHeader(String arg0) {
        return false;
    }

    public String encodeRedirectURL(String arg0) {
        return null;
    }

    public String encodeRedirectUrl(String arg0) {
        return null;
    }

    public String encodeURL(String arg0) {
        return null;
    }

    public String encodeUrl(String arg0) {
        return null;
    }

    public void sendError(int arg0) throws IOException {
    }

    public void sendError(int arg0, String arg1) throws IOException {
    }

    public void sendRedirect(String arg0) throws IOException {
    }

    public void setDateHeader(String arg0, long arg1) {
    }

    public void setHeader(String arg0, String arg1) {
    }

    public void setIntHeader(String arg0, int arg1) {
    }

    public void setStatus(int arg0, String arg1) {
    }

    public void flushBuffer() throws IOException {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public String getContentType() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    public void setOutputStream(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer() {
    }

    public void setBufferSize(int arg0) {
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public void setContentLength(int arg0) {
    }

    public void setContentType(String arg0) {
    }

    public void setLocale(Locale arg0) {
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public Collection<String> getHeaders(String name) {
        String val = headerMap.get(name);
        List list = new ArrayList();
        if (val != null)
            list.add(val);
        return list;
    }

    public Collection<String> getHeaderNames() {
        return headerMap.keySet();
    }

    public long getContentCount() {
        return DUMMY_DEFAULT_CONTENT_COUNT;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
