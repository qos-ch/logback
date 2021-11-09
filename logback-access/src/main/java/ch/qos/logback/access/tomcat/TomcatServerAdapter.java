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

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import ch.qos.logback.access.spi.ServerAdapter;

/**
 * A tomcat specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 */
public class TomcatServerAdapter implements ServerAdapter {

    Request request;
    Response response;

    public TomcatServerAdapter(final Request tomcatRequest, final Response tomcatResponse) {
        request = tomcatRequest;
        response = tomcatResponse;
    }

    @Override
    public long getContentLength() {
        return response.getContentLength();
    }

    @Override
    public int getStatusCode() {
        return response.getStatus();
    }

    @Override
    public long getRequestTimestamp() {
        return request.getCoyoteRequest().getStartTime();
    }

    @Override
    public Map<String, String> buildResponseHeaderMap() {
        final Map<String, String> responseHeaderMap = new HashMap<>();
        for (final String key : response.getHeaderNames()) {
            final String value = response.getHeader(key);
            responseHeaderMap.put(key, value);
        }
        return responseHeaderMap;
    }
}
