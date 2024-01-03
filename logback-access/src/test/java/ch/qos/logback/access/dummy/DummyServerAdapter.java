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

import ch.qos.logback.access.spi.ServletApiServerAdapter;

public class DummyServerAdapter extends ServletApiServerAdapter {

    DummyResponse response;

    public DummyServerAdapter(DummyRequest dummyRequest, DummyResponse dummyResponse) {
        super(dummyRequest, dummyResponse);
        this.response = dummyResponse;
    }

    public long getResponseContentLength() {
        return response.getContentCount();
    }

    public int getStatusCode() {
        return response.getStatus();
    }

    public long getRequestTimestamp() {
        return -1;
    }
}
