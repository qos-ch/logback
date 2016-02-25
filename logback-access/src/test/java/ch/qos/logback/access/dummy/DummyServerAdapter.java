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

import ch.qos.logback.access.spi.ServerAdapter;

import java.util.Map;

public class DummyServerAdapter implements ServerAdapter {

    DummyRequest request;
    DummyResponse response;

    public DummyServerAdapter(DummyRequest dummyRequest, DummyResponse dummyResponse) {
        this.request = dummyRequest;
        this.response = dummyResponse;
    }

    public long getContentLength() {
        return response.getContentCount();
    }

    public int getStatusCode() {
        return response.getStatus();
    }

    public long getRequestTimestamp() {
        return -1;
    }

    public Map<String, String> buildResponseHeaderMap() {
        return response.headerMap;
    }

}
