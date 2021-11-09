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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class TeeHttpServletResponse extends HttpServletResponseWrapper {

    TeeServletOutputStream teeServletOutputStream;
    PrintWriter teeWriter;

    public TeeHttpServletResponse(final HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (teeServletOutputStream == null) {
            teeServletOutputStream = new TeeServletOutputStream(getResponse());
        }
        return teeServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (teeWriter == null) {
            teeWriter = new PrintWriter(new OutputStreamWriter(getOutputStream(), getResponse().getCharacterEncoding()), true);
        }
        return teeWriter;
    }

    @Override
    public void flushBuffer() {
        if (teeWriter != null) {
            teeWriter.flush();
        }
    }

    byte[] getOutputBuffer() {
        // teeServletOutputStream can be null if the getOutputStream method is never
        // called.
        if (teeServletOutputStream != null) {
            return teeServletOutputStream.getOutputStreamAsByteArray();
        }
        return null;
    }

    void finish() throws IOException {
        if (teeWriter != null) {
            teeWriter.close();
        }
        if (teeServletOutputStream != null) {
            teeServletOutputStream.close();
        }
    }
}
