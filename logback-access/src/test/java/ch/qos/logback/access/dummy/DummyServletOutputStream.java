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
import java.io.OutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

public class DummyServletOutputStream extends ServletOutputStream {

	private final OutputStream targetStream;

	public DummyServletOutputStream(final OutputStream targetStream) {
		this.targetStream = targetStream;
	}

	@Override
	public void write(final int b) throws IOException {
		targetStream.write(b);
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		targetStream.flush();
	}

	@Override
	public void close() throws IOException {
		super.close();
		targetStream.close();
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(final WriteListener listener) {
		// TODO Auto-generated method stub

	}
}
