/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.classic.blackbox.joran.spi;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileServlet  extends HttpServlet {

    Map<String, String> headers = new HashMap<String, String>();
    static final String DEFAULT_CONTENT =  "That was all";
    String contents;
    static final String LAST_MODIFIED = "last-modified";
    public final static String CONTENT_KEY = "content";

    public ConfigFileServlet(String contents) {
        this.contents = contents;
    }

    public ConfigFileServlet() {
       this(DEFAULT_CONTENT);
    }

    /**
     * Returns data set when {@link #doPost(HttpServletRequest, HttpServletResponse)} was called
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/txt;charset=utf-8");

        String lastModifiedHeaderValue = headers.get(LAST_MODIFIED);
        if(lastModifiedHeaderValue != null) {
            response.setHeader(LAST_MODIFIED, lastModifiedHeaderValue);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(contents);
    }

    /**
     * Remembers posted values to be returned when {@link #doGet(HttpServletRequest, HttpServletResponse)} is called.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            // Handle single or multiple values for the parameter
            if (paramValues.length >= 1) {
                if(CONTENT_KEY.equals(paramName)) {
                     contents = paramValues[0];
                } else {
                    headers.put(paramName, paramValues[0]);
                }
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(contents);
        }


    }

}
