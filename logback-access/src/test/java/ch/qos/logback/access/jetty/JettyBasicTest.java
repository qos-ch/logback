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
package ch.qos.logback.access.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.Util;
import ch.qos.logback.access.testUtil.NotifyingListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;

public class JettyBasicTest {

    static RequestLogImpl REQUEST_LOG_IMPL;
    static JettyFixtureWithListAndConsoleAppenders JETTY_FIXTURE;

    private static final int TIMEOUT = 5;
    static int RANDOM_SERVER_PORT = RandomUtil.getRandomServerPort();

    @BeforeClass
    static public void startServer() throws Exception {
        REQUEST_LOG_IMPL = new RequestLogImpl();
        JETTY_FIXTURE = new JettyFixtureWithListAndConsoleAppenders(REQUEST_LOG_IMPL, RANDOM_SERVER_PORT);
        JETTY_FIXTURE.start();
    }

    @AfterClass
    static public void stopServer() throws Exception {
        if (JETTY_FIXTURE != null) {
            JETTY_FIXTURE.stop();
        }
    }

    @Test
    public void getRequest() throws Exception {
        URL url = new URL("http://localhost:" + RANDOM_SERVER_PORT + "/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);

        String result = Util.readToString(connection.getInputStream());

        assertEquals("hello world", result);

        NotifyingListAppender listAppender = (NotifyingListAppender) REQUEST_LOG_IMPL.getAppender("list");
        listAppender.list.clear();
    }

    @Test
    public void eventGoesToAppenders() throws Exception {
        URL url = new URL(JETTY_FIXTURE.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);

        String result = Util.readToString(connection.getInputStream());

        assertEquals("hello world", result);

        NotifyingListAppender listAppender = (NotifyingListAppender) REQUEST_LOG_IMPL.getAppender("list");
        IAccessEvent event = listAppender.list.poll(TIMEOUT, TimeUnit.SECONDS);
        assertNotNull("No events received", event);

        assertEquals("127.0.0.1", event.getRemoteHost());
        assertEquals("localhost", event.getServerName());
        listAppender.list.clear();
    }

    @Test
    public void postContentConverter() throws Exception {
        URL url = new URL(JETTY_FIXTURE.getUrl());
        String msg = "test message";

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // this line is necessary to make the stream aware of when the message is
        // over.
        connection.setFixedLengthStreamingMode(msg.getBytes().length);
        ((HttpURLConnection) connection).setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type", "text/plain");

        PrintWriter output = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
        output.print(msg);
        output.flush();
        output.close();

        // StatusPrinter.print(requestLogImpl.getStatusManager());

        NotifyingListAppender listAppender = (NotifyingListAppender) REQUEST_LOG_IMPL.getAppender("list");

        IAccessEvent event = listAppender.list.poll(TIMEOUT, TimeUnit.SECONDS);
        assertNotNull("No events received", event);

        // we should test the contents of the requests
        // assertEquals(msg, event.getRequestContent());
    }
}
