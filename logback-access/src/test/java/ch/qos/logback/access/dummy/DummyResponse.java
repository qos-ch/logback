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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class DummyResponse implements HttpServletResponse {

	public static final int DUMMY_DEFAULT_STATUS = 200;
	public static final int DUMMY_DEFAULT_CONTENT_COUNT = 1000;
	public static final Map<String, String> DUMMY_DEFAULT_HDEADER_MAP = new HashMap<>();

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

	@Override
	public void addCookie(final Cookie arg0) {
	}

	@Override
	public void addDateHeader(final String arg0, final long arg1) {
	}

	@Override
	public void addHeader(final String arg0, final String arg1) {
	}

	@Override
	public void addIntHeader(final String arg0, final int arg1) {
	}

	@Override
	public boolean containsHeader(final String arg0) {
		return false;
	}

	@Override
	public String encodeRedirectURL(final String arg0) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(final String arg0) {
		return null;
	}

	@Override
	public String encodeURL(final String arg0) {
		return null;
	}

	@Override
	public String encodeUrl(final String arg0) {
		return null;
	}

	@Override
	public void sendError(final int arg0) throws IOException {
	}

	@Override
	public void sendError(final int arg0, final String arg1) throws IOException {
	}

	@Override
	public void sendRedirect(final String arg0) throws IOException {
	}

	@Override
	public void setDateHeader(final String arg0, final long arg1) {
	}

	@Override
	public void setHeader(final String arg0, final String arg1) {
	}

	@Override
	public void setIntHeader(final String arg0, final int arg1) {
	}

	@Override
	public void setStatus(final int arg0, final String arg1) {
	}

	@Override
	public void flushBuffer() throws IOException {
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	public void setOutputStream(final ServletOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public void setBufferSize(final int arg0) {
	}

	@Override
	public void setCharacterEncoding(final String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public void setContentLength(final int arg0) {
	}

	@Override
	public void setContentType(final String arg0) {
	}

	@Override
	public void setLocale(final Locale arg0) {
	}

	@Override
	public String getHeader(final String key) {
		return headerMap.get(key);
	}

	@Override
	public Collection<String> getHeaders(final String name) {
		final String val = headerMap.get(name);
		final List<String> list = new ArrayList<>();
		if (val != null) {
			list.add(val);
		}
		return list;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headerMap.keySet();
	}

	public long getContentCount() {
		return DUMMY_DEFAULT_CONTENT_COUNT;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(final int status) {
		this.status = status;
	}

	@Override
	public void setContentLengthLong(final long length) {
		// TODO Auto-generated method stub
	}

}
