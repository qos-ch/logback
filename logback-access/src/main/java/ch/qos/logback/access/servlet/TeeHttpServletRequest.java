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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import static ch.qos.logback.access.AccessConstants.LB_INPUT_BUFFER;

/**
 * As the "tee" program on Unix, duplicate the request's input stream.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
class TeeHttpServletRequest extends HttpServletRequestWrapper {

    private TeeServletInputStream inStream;
    private BufferedReader reader;
    boolean postedParametersMode = false;

    TeeHttpServletRequest(HttpServletRequest request) {
        super(request);
        // we can't access the input stream and access the request parameters
        // at the same time
        if (Util.isFormUrlEncoded(request)) {
            postedParametersMode = true;
        } else {
            inStream = new TeeServletInputStream(request);
            // add the contents of the input buffer as an attribute of the request
            request.setAttribute(LB_INPUT_BUFFER, inStream.getInputBuffer());
            reader = new BufferedReader(new InputStreamReader(inStream));
        }

    }

    byte[] getInputBuffer() {
        if (postedParametersMode) {
            throw new IllegalStateException("Call disallowed in postedParametersMode");
        }
        return inStream.getInputBuffer();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!postedParametersMode) {
            return inStream;
        } else {
            return super.getInputStream();
        }
    }

    //

    @Override
    public BufferedReader getReader() throws IOException {
        if (!postedParametersMode) {
            return reader;
        } else {
            return super.getReader();
        }
    }

    public boolean isPostedParametersMode() {
        return postedParametersMode;
    }

}
