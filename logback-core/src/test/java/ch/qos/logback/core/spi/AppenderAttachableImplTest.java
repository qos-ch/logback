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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.helpers.NOPAppender;

/**
 * This test case verifies all the methods of AppenderAttableImpl work properly.
 *
 * @author Ralph Goers
 */
public class AppenderAttachableImplTest {

    private AppenderAttachableImpl<TestEvent> aai;

    @Before
    public void setUp() throws Exception {
        aai = new AppenderAttachableImpl<TestEvent>();
    }

    @After
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
        assertTrue("Incorrect number of appenders", size == 2);
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
            assertTrue("Bad Appender", app == ta || app == tab);
        }
        assertTrue("Incorrect number of appenders", size == 2);
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
        assertNotNull("Could not find appender", a);
        assertTrue("Wrong appender", a == testOther);

        a = aai.getAppender("test");
        assertNotNull("Could not find appender", a);
        assertTrue("Wrong appender", a == test);
        a = aai.getAppender("NotThere");
        assertNull("Appender was returned", a);
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
        assertTrue("Appender is not attached", aai.isAttached(ta));
        assertTrue("Appender is not attached", aai.isAttached(tab));
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
        assertTrue("Appender was not started", tab.isStarted());
        aai.detachAndStopAllAppenders();
        assertNull("Appender was not removed", aai.getAppender("test"));
        assertFalse("Appender was not stopped", tab.isStarted());
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
        assertTrue("Appender not detached", aai.detachAppender(tab));
        assertNull("Appender was not removed", aai.getAppender("test"));
        assertFalse("Appender detach error", aai.detachAppender(tab));
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

        assertTrue(aai.detachAppender("test"));
        assertTrue(aai.detachAppender("test1"));
        assertFalse(aai.detachAppender("test1"));
    }

    private static class TestEvent {

    }

}
