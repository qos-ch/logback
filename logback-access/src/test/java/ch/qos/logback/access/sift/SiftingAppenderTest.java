/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.Util;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class SiftingAppenderTest {
  static final String PREFIX = "src/test/input/jetty/";
  static int RANDOM_SERVER_PORT = RandomUtil.getRandomServerPort();

  JettyFixture jettyFixture;
  RequestLogImpl rli = new RequestLogImpl();

  @Before
  public void startServer() throws Exception {
    jettyFixture = new JettyFixture(rli, RANDOM_SERVER_PORT);

  }

  @After
  public void stopServer() throws Exception {
    if (jettyFixture != null) {
      jettyFixture.stop();
    }
  }

  @Test
  public void test() throws Exception {
    rli.setFileName(PREFIX + "sifting.xml");
    jettyFixture.start();


    StatusPrinter.print(rli);
    invokeServer("/");
    invokeServer("/x");
    invokeServer("/x");
    invokeServer("/y");

    
    SiftingAppender siftingAppender = (SiftingAppender) rli
        .getAppender("SIFTING");
    List<String> keyList = siftingAppender.getAppenderTracker().keyList();
    assertEquals(3, keyList.size());

    List<String> witnessList = new ArrayList<String>();
    witnessList.add("NA");
    witnessList.add("x");
    witnessList.add("y");
    assertEquals(witnessList, keyList);

    long now = System.currentTimeMillis();
    {
      ListAppender<AccessEvent> listAppender = (ListAppender<AccessEvent>) siftingAppender
          .getAppenderTracker().get("NA", now);
      assertEquals(1, listAppender.list.size());
    }
    
    {
      ListAppender<AccessEvent> listAppender = (ListAppender<AccessEvent>) siftingAppender
          .getAppenderTracker().get("x", now);
      assertEquals(2, listAppender.list.size());
    }
    {
      ListAppender<AccessEvent> listAppender = (ListAppender<AccessEvent>) siftingAppender
          .getAppenderTracker().get("y", now);
      assertEquals(1, listAppender.list.size());
    }
  }

  void invokeServer(String uri) throws Exception {
    URL url = new URL("http://localhost:" + RANDOM_SERVER_PORT + uri);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);
    Util.readToString(connection.getInputStream());
    Thread.sleep(30);
  }
}
