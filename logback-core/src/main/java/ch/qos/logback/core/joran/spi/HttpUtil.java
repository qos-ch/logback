/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.joran.spi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    URL url;

    public enum RequestMethod {
        GET,
        POST;
    }


    HttpURLConnection conn;
    RequestMethod requestMethod;

    Map<String, String> headerMap = new HashMap<>(2);

    public HttpUtil(RequestMethod requestMethod, URL url) {
        this.requestMethod = requestMethod;
        this.url =url;
    }

    public HttpUtil(RequestMethod requestMethod, String urlStr) throws MalformedURLException {
        this(requestMethod, new URL(urlStr));
    }

    Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public HttpURLConnection connectTextTxt() {
        return connectType( "text/txt;charset=utf-8");
    }

    public HttpURLConnection connectTextPlain() {
        return connectType("text/plain; charset=utf-8");
    }

    public HttpURLConnection connectType(String acceptType) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod.name());
            headerMap.forEach((h, v) -> conn.setRequestProperty(h, v));
            conn.setRequestProperty("Accept", acceptType);

            if(requestMethod == RequestMethod.POST) {
                conn.setDoOutput(true);
            }

            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String readResponse(HttpURLConnection conn) {
        if(conn == null)
            return null;

        try {
            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                return innerReadResponse(conn);
            } else {
                System.out.println("status="+ responseCode+ " Failed response");
                return null;
            }
        } catch (IOException e) {
            System.out.println("url="+ url.toString()+" failed to read status");
            e.printStackTrace();
            return  null;
        }
    }

    private String innerReadResponse(HttpURLConnection conn) {
        try (InputStream is = conn.getInputStream()) {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String inputLine;
            StringBuffer buffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                buffer.append(inputLine);
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean post(HttpURLConnection conn, String str) {
        if (conn == null) {
            System.out.println("null HttpURLConnection object");
            return false;
        }

        if(requestMethod != RequestMethod.POST) {
            System.out.println("Incorrect request method "+requestMethod.name());
            return false;
        }

        try (OutputStream os = conn.getOutputStream()) {
            OutputStreamWriter wr = new OutputStreamWriter(os);
            wr.write(str);
            wr.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
