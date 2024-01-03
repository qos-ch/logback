/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import ch.qos.logback.access.spi.ServletApiServerAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A Jetty pre 12 specific implementation of the {@link ServerAdapter} interface.
 *
 * @author S&eacute;bastien Pennec
 * @author Ceki Gulcu
 * @author Joakim Erdfelt
 */
public class JettyServletApiServerAdapter extends ServletApiServerAdapter {

    private final Response response;
    private final Request request;
    private static final Method responseGetHttpChannel;
    private static final Method httpChannelGetBytesWritten;
    private static final Method responseGetCommittedMetaData;
    private static final Method committedMetadataGetStatus;
    private static final Method requestGetTimeStamp;
    private static final Method responseGetHttpFields;

    static {
        // Since we can only have one version of jetty as dependency on compile time we need to
        // access methods that are no longer available up from jetty 12 via injection here.
        responseGetHttpChannel = getMethod(Response.class, "getHttpChannel");
        httpChannelGetBytesWritten = getMethod(HttpChannel.class, "getBytesWritten");
        responseGetCommittedMetaData = getMethod(Response.class, "getCommittedMetaData");
        responseGetHttpFields = getMethod(Response.class, "getHttpFields");
        committedMetadataGetStatus = getMethod(MetaData.Response.class, "getStatus");
        requestGetTimeStamp = getMethod(Request.class, "getTimeStamp");
    }

    private static Method getMethod(Class<?> sourceClass, String methodName) {
        try {
            return sourceClass.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            // should not happen, but we don't want to fail the logging due to an error here
            e.printStackTrace();
            return null;
        }
    }

    public JettyServletApiServerAdapter(Request jettyRequest, Response jettyResponse) {
        super((HttpServletRequest) jettyRequest, (HttpServletResponse) jettyResponse);
        this.request = jettyRequest;
        this.response = jettyResponse;
    }

    @Override
    public long getResponseContentLength() {
        // response.getHttpChannel().getBytesWritten()
        if (responseGetHttpChannel != null && httpChannelGetBytesWritten != null) {
            try {
                Object httpChannel = responseGetHttpChannel.invoke(response);
                return (long) httpChannelGetBytesWritten.invoke(httpChannel);
            } catch (InvocationTargetException | IllegalAccessException e) {
                // should not happen, but we don't want to fail the logging due to an error here
                e.printStackTrace();
            }
        }
        return IAccessEvent.SENTINEL;
    }

    @Override
    public int getStatusCode() {
        // response.getCommittedMetaData().getStatus()
        if (responseGetCommittedMetaData != null && committedMetadataGetStatus != null) {
            try {
                Object committedMetaData = responseGetCommittedMetaData.invoke(response);
                return (int) committedMetadataGetStatus.invoke(committedMetaData);
            } catch (InvocationTargetException | IllegalAccessException e) {
                // should not happen, but we don't want to fail the logging due to an error here
                e.printStackTrace();
            }
        }
        return IAccessEvent.SENTINEL;
    }

    @Override
    public long getRequestTimestamp() {
        // request.getTimeStamp()
        if (requestGetTimeStamp != null) {
            try {
                return (long) requestGetTimeStamp.invoke(request);
            } catch (InvocationTargetException | IllegalAccessException e) {
                // should not happen, but we don't want to fail the logging due to an error here
                e.printStackTrace();
            }
        }
        return IAccessEvent.SENTINEL;
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        Map<String, String> responseHeaderMap = new HashMap<>();

        try {
            Iterator<HttpField> httpFieldIter = ((HttpFields) responseGetHttpFields.invoke(response)).iterator();
            while (httpFieldIter.hasNext()) {
                HttpField httpField = httpFieldIter.next();
                String key = httpField.getName();
                String value = httpField.getValue();
                responseHeaderMap.put(key, value);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            // should not happen, but we don't want to fail the logging due to an error here
            e.printStackTrace();
        }
        return responseHeaderMap;
    }
}
