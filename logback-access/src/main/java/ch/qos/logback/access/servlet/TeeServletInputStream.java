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
package ch.qos.logback.access.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

class TeeServletInputStream extends ServletInputStream {

	InputStream in;
	byte[] inputBuffer;

	TeeServletInputStream(final HttpServletRequest request) {
		duplicateInputStream(request);
	}

	private void duplicateInputStream(final HttpServletRequest request) {
		ServletInputStream originalSIS = null;
		try {
			originalSIS = request.getInputStream();
			inputBuffer = consumeBufferAndReturnAsByteArray(originalSIS);
			in = new ByteArrayInputStream(inputBuffer);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			closeStream(originalSIS);
		}
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	byte[] consumeBufferAndReturnAsByteArray(final InputStream is) throws IOException {
		final int len = 1024;
		final byte[] temp = new byte[len];
		int c = -1;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((c = is.read(temp, 0, len)) != -1) {
			baos.write(temp, 0, c);
		}
		return baos.toByteArray();
	}

	void closeStream(final ServletInputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (final IOException e) {
			}
		}
	}

	byte[] getInputBuffer() {
		return inputBuffer;
	}

	@Override
	public boolean isFinished() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean isReady() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void setReadListener(final ReadListener listener) {
		throw new RuntimeException("Not yet implemented");
	}
}
