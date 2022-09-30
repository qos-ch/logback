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
package ch.qos.logback.access.sift;

import ch.qos.logback.access.jetty.JettyFixtureBase;
import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.Util;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiftingAppenderTest {
    static final String PREFIX = "src/test/input/jetty/";
    static int RANDOM_SERVER_PORT = RandomUtil.getRandomServerPort();

    JettyFixtureBase jettyFixture;
    RequestLogImpl rli = new RequestLogImpl();

    @BeforeEach
    public void startServer() throws Exception {
        jettyFixture = new JettyFixtureBase(rli, RANDOM_SERVER_PORT);
    }

    @AfterEach
    public void stopServer() throws Exception {
        if (jettyFixture != null) {
            jettyFixture.stop();
        }
    }

    @Test
    public void invokingDifferentPathShouldBeSiftedAccordingly() throws Exception {
        rli.setFileName(PREFIX + "sifting.xml");
        jettyFixture.start();
        invokeServer("/");
        invokeServer("/x");
        invokeServer("/x");
        invokeServer("/y");

        StatusPrinter.print(rli);
        SiftingAppender siftingAppender = (SiftingAppender) rli.getAppender("SIFTING");
        Set<String> keySet = siftingAppender.getAppenderTracker().allKeys();
        assertEquals(3, keySet.size());

        Set<String> witnessSet = new LinkedHashSet<String>();
        witnessSet.add("NA");
        witnessSet.add("x");
        witnessSet.add("y");
        assertEquals(witnessSet, keySet);

        check(siftingAppender, "NA", 1);
        check(siftingAppender, "x", 2);
        check(siftingAppender, "y", 1);
    }

    private void check(SiftingAppender siftingAppender, String key, int expectedCount) {
        ListAppender<IAccessEvent> listAppender = (ListAppender<IAccessEvent>) siftingAppender.getAppenderTracker().find(key);
        assertEquals(expectedCount, listAppender.list.size());
    }

    void invokeServer(String uri) throws Exception {
        URL url = new URL("http://localhost:" + RANDOM_SERVER_PORT + uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        Util.readToString(connection.getInputStream());
        Thread.sleep(10);
    }
}
