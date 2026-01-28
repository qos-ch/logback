/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.jetty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.qos.logback.access.spi.ServerAdapter;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 * A Jetty 9.4.x and 10.0.x specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 * @author Ceki Gulcu
 * @author Joakim Erdfelt
 */
public class JettyModernServerAdapter extends JettyServerAdapter {
    private static final Method RESPONSE_GET_HTTP_FIELDS;

    static {
        try {
            RESPONSE_GET_HTTP_FIELDS = Response.class.getDeclaredMethod("getHttpFields");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public JettyModernServerAdapter(Request jettyRequest, Response jettyResponse) {
        super(jettyRequest, jettyResponse);
    }

    @Override
    public long getContentLength() {
        return response.getHttpChannel().getBytesWritten();
    }

    @Override
    public int getStatusCode() {
        return response.getCommittedMetaData().getStatus();
    }

    @Override
    public long getRequestTimestamp() {
        return request.getTimeStamp();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> buildResponseHeaderMap() {
        Map<String, String> responseHeaderMap = new HashMap<String, String>();
        Iterable<HttpField> httpFields;
        try {
            httpFields = (Iterable<HttpField>) RESPONSE_GET_HTTP_FIELDS.invoke(response);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        Iterator<HttpField> httpFieldIter = httpFields.iterator();
        while (httpFieldIter.hasNext()) {
            HttpField httpField = httpFieldIter.next();
            String key = httpField.getName();
            String value = httpField.getValue();
            responseHeaderMap.put(key, value);
        }
        return responseHeaderMap;
    }

}
