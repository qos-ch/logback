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
package ch.qos.logback.core.spi;

import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.helpers.NOPAppender;
import org.junit.jupiter.api.Test;

/**
 * This test case verifies all the methods of AppenderAttableImpl work properly.
 *
 * @author Ralph Goers
 */
public class AppenderAttachableImplTest {

    private AppenderAttachableImpl<TestEvent> aai;

    @BeforeEach
    public void setUp() throws Exception {
        aai = new AppenderAttachableImpl<TestEvent>();
    }

    @AfterEach
    public void tearDown() throws Exception {
        aai = null;
    }

    @Test
    public void testAddAppender() throws Exception {
        TestEvent event = new TestEvent();
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.start();
        aai.addAppender(ta);
        ta = new NOPAppender<TestEvent>();
        ta.setName("test");
        ta.start();
        aai.addAppender(ta);
        int size = aai.appendLoopOnAppenders(event);
        Assertions.assertTrue(size == 2, "Incorrect number of appenders");
    }

    @Test
    public void testIteratorForAppenders() throws Exception {
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.start();
        aai.addAppender(ta);
        NOPAppender<TestEvent> tab = new NOPAppender<TestEvent>();
        tab.setName("test");
        tab.start();
        aai.addAppender(tab);
        Iterator<Appender<TestEvent>> iter = aai.iteratorForAppenders();
        int size = 0;
        while (iter.hasNext()) {
            ++size;
            Appender<TestEvent> app = iter.next();
            Assertions.assertTrue(app == ta || app == tab, "Bad Appender");
        }
        Assertions.assertTrue(size == 2, "Incorrect number of appenders");
    }

    @Test
    public void getGetAppender() throws Exception {
        NOPAppender<TestEvent> test = new NOPAppender<TestEvent>();
        test.setName("test");
        test.start();
        aai.addAppender(test);

        NOPAppender<TestEvent> testOther = new NOPAppender<TestEvent>();
        testOther.setName("testOther");
        testOther.start();
        aai.addAppender(testOther);

        Appender<TestEvent> a = aai.getAppender("testOther");
        Assertions.assertNotNull(a, "Could not find appender");
        Assertions.assertTrue(a == testOther, "Wrong appender");

        a = aai.getAppender("test");
        Assertions.assertNotNull(a, "Could not find appender");
        Assertions.assertTrue(a == test, "Wrong appender");
        a = aai.getAppender("NotThere");
        Assertions.assertNull(a, "Appender was returned");
    }

    @Test
    public void testIsAttached() throws Exception {
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.start();
        aai.addAppender(ta);
        NOPAppender<TestEvent> tab = new NOPAppender<TestEvent>();
        tab.setName("test");
        tab.start();
        aai.addAppender(tab);
        Assertions.assertTrue(aai.isAttached(ta), "Appender is not attached");
        Assertions.assertTrue(aai.isAttached(tab), "Appender is not attached");
    }

    @Test
    public void testDetachAndStopAllAppenders() throws Exception {
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.start();
        aai.addAppender(ta);
        NOPAppender<TestEvent> tab = new NOPAppender<TestEvent>();
        tab.setName("test");
        tab.start();
        aai.addAppender(tab);
        Assertions.assertTrue(tab.isStarted(), "Appender was not started");
        aai.detachAndStopAllAppenders();
        Assertions.assertNull(aai.getAppender("test"), "Appender was not removed");
        Assertions.assertFalse(tab.isStarted(), "Appender was not stopped");
    }

    @Test
    public void testDetachAppender() throws Exception {
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.start();
        aai.addAppender(ta);
        NOPAppender<TestEvent> tab = new NOPAppender<TestEvent>();
        tab.setName("test");
        tab.start();
        aai.addAppender(tab);
        Assertions.assertTrue(aai.detachAppender(tab),"Appender not detached");
        Assertions.assertNull(aai.getAppender("test"), "Appender was not removed");
        Assertions.assertFalse(aai.detachAppender(tab), "Appender detach error");
    }

    @Test
    public void testDetachAppenderByName() throws Exception {
        NOPAppender<TestEvent> ta = new NOPAppender<TestEvent>();
        ta.setName("test1");
        ta.start();
        aai.addAppender(ta);
        NOPAppender<TestEvent> tab = new NOPAppender<TestEvent>();
        tab.setName("test");
        tab.start();
        aai.addAppender(tab);

        Assertions.assertTrue(aai.detachAppender("test"));
        Assertions.assertTrue(aai.detachAppender("test1"));
        Assertions.assertFalse(aai.detachAppender("test1"));
    }

    private static class TestEvent {

    }

}
