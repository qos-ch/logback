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
package ch.qos.logback.access.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.AccessTestConstants;
import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.StringListAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class JoranConfiguratorTest {

    AccessContext context = new AccessContext();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        jc.doConfigure(file);
    }

    @Test
    public void smoke() throws Exception {
        configure(AccessTestConstants.TEST_DIR_PREFIX + "input/joran/smoke.xml");
        StatusPrinter.print(context);
        ListAppender<IAccessEvent> listAppender = (ListAppender<IAccessEvent>) context.getAppender("LIST");
        assertNotNull(listAppender);
        IAccessEvent event = DummyAccessEventBuilder.buildNewAccessEvent();
        listAppender.doAppend(event);

        assertEquals(1, listAppender.list.size());

        assertEquals(1, listAppender.list.size());
        IAccessEvent ae = listAppender.list.get(0);
        assertNotNull(ae);
    }

    @Test
    public void defaultLayout() throws Exception {
        configure(AccessTestConstants.TEST_DIR_PREFIX + "input/joran/defaultLayout.xml");
        StringListAppender<IAccessEvent> listAppender = (StringListAppender<IAccessEvent>) context.getAppender("STR_LIST");
        IAccessEvent event = DummyAccessEventBuilder.buildNewAccessEvent();
        listAppender.doAppend(event);
        assertEquals(1, listAppender.strList.size());
        // the result contains a line separator at the end
        assertTrue(listAppender.strList.get(0).startsWith("testMethod"));
    }
}
