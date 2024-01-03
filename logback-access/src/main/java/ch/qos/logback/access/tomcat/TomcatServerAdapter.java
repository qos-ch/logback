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
package ch.qos.logback.access.tomcat;

import ch.qos.logback.access.spi.ServerAdapter;

import ch.qos.logback.access.spi.ServletApiServerAdapter;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * A tomcat specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class TomcatServerAdapter extends ServletApiServerAdapter {

    Request request;
    Response response;

    public TomcatServerAdapter(Request tomcatRequest, Response tomcatResponse) {
        super(tomcatRequest, tomcatResponse);
        this.request = tomcatRequest;
        this.response = tomcatResponse;
    }

    @Override
    public long getResponseContentLength() {
        return response.getContentLength();
    }

    @Override
    public long getRequestTimestamp() {
        return request.getCoyoteRequest().getStartTime();
    }
}
