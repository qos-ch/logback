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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Relatively straightforward unit tests for AppenderTracker.
 */
public class AppenderTrackerTest {

    Context context = new ContextBase();
    ListAppenderFactory listAppenderFactory = new ListAppenderFactory();
    int diff = RandomUtil.getPositiveInt();
    AppenderTracker<Object> appenderTracker = new AppenderTracker<Object>(context, listAppenderFactory);
    String key = "k-" + diff;
    long now = 3000;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void removeStaleComponentsShouldNotBomb() {
        appenderTracker.removeStaleComponents(now);
        Assertions.assertEquals(0, appenderTracker.getComponentCount());
    }

    @Test
    public void findingTheInexistentShouldNotBomb() {
        Assertions.assertNull(appenderTracker.find(key));
        now += AppenderTracker.DEFAULT_TIMEOUT + 1;
        appenderTracker.removeStaleComponents(now);
        Assertions.assertNull(appenderTracker.find(key));
    }

    @Test
    public void smoke() {
        Appender<Object> a = appenderTracker.getOrCreate(key, now);
        Assertions.assertTrue(a.isStarted());
        now += AppenderTracker.DEFAULT_TIMEOUT + 1;
        appenderTracker.removeStaleComponents(now);
        Assertions.assertFalse(a.isStarted());
        Assertions.assertNull(appenderTracker.find(key));
    }

    @Test
    public void endOfLivedAppendersShouldBeRemovedAfterLingeringTimeout() {
        Appender<Object> a = appenderTracker.getOrCreate(key, now);
        appenderTracker.endOfLife(key);
        now += AppenderTracker.LINGERING_TIMEOUT + 1;
        appenderTracker.removeStaleComponents(now);
        Assertions.assertFalse(a.isStarted());
        a = appenderTracker.find(key);
        Assertions.assertNull(a);
    }

    @Test
    public void endOfLivedAppenderShouldBeAvailableDuringLingeringPeriod() {
        Appender<Object> a = appenderTracker.getOrCreate(key, now);
        appenderTracker.endOfLife(key);
        // clean
        appenderTracker.removeStaleComponents(now);
        Appender<Object> lingering = appenderTracker.getOrCreate(key, now);
        Assertions.assertTrue(lingering.isStarted());
        Assertions.assertTrue(a == lingering);
        now += AppenderTracker.LINGERING_TIMEOUT + 1;
        appenderTracker.removeStaleComponents(now);
        Assertions.assertFalse(a.isStarted());
        a = appenderTracker.find(key);
        Assertions.assertNull(a);
    }

    @Test
    public void trackerShouldHonorMaxComponentsParameter() {
        List<Appender<Object>> appenderList = new ArrayList<>();
        int max = 10;
        appenderTracker.setMaxComponents(max);
        for (int i = 0; i < (max + 1); i++) {
            Appender<Object> a = appenderTracker.getOrCreate(key + "-" + i, now++);
            appenderList.add(a);
        }
        // cleaning only happens in removeStaleComponents
        appenderTracker.removeStaleComponents(now++);
        Assertions.assertEquals(max, appenderTracker.allKeys().size());
        Assertions.assertNull(appenderTracker.find(key + "-" + 0));
        Assertions.assertFalse(appenderList.get(0).isStarted());
    }

    @Test
    public void trackerShouldHonorTimeoutParameter() {
        List<Appender<Object>> appenderList = new ArrayList<Appender<Object>>();
        int timeout = 2;
        appenderTracker.setTimeout(timeout);
        for (int i = 0; i <= timeout; i++) {
            Appender<Object> a = appenderTracker.getOrCreate(key + "-" + i, now++);
            appenderList.add(a);
        }

        long numComponentsCreated = timeout + 1;
        Assertions.assertEquals(numComponentsCreated, appenderTracker.allKeys().size());

        // cleaning only happens in removeStaleComponents. The first appender should
        // time out
        appenderTracker.removeStaleComponents(now++);

        // the first appender should have been removed
        Assertions.assertEquals(numComponentsCreated - 1, appenderTracker.allKeys().size());
        Assertions.assertNull(appenderTracker.find(key + "-" + 0));
        Assertions.assertFalse(appenderList.get(0).isStarted());

        // the other appenders should be in the tracker
        for (int i = 1; i <= timeout; i++) {
            Assertions.assertNotNull(appenderTracker.find(key + "-" + i));
            Assertions.assertTrue(appenderList.get(i).isStarted());
        }
    }

    // ======================================================================
    static class ListAppenderFactory implements AppenderFactory<Object> {

        public Appender<Object> buildAppender(Context context, String discriminatingValue) throws JoranException {
            ListAppender<Object> la = new ListAppender<Object>();
            la.setContext(context);
            la.setName(discriminatingValue);
            la.start();
            return la;
        }
    }
}
