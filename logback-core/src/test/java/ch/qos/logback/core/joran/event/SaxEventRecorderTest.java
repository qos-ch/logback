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
package ch.qos.logback.core.joran.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.Attributes;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.StatusChecker;

/**
 * Test whether SaxEventRecorder does a good job.
 * 
 * @author Ceki Gulcu
 */
public class SaxEventRecorderTest {

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);

    SAXParser createParser() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        return spf.newSAXParser();
    }

    public List<SaxEvent> doTest(String filename) throws Exception {
        SaxEventRecorder recorder = new SaxEventRecorder(context);
        FileInputStream fis = new FileInputStream(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/" + filename);
        recorder.recordEvents(fis);
        return recorder.getSaxEventList();

    }

    public void dump(List<SaxEvent> seList) {
        for (SaxEvent se : seList) {
            System.out.println(se);
        }
    }

    @Test
    public void test1() throws Exception {
        List<SaxEvent> seList = doTest("event1.xml");
        assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        assertEquals(11, seList.size());
    }

    @Test
    public void test2() throws Exception {
        List<SaxEvent> seList = doTest("ampEvent.xml");
        assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        assertEquals(3, seList.size());

        BodyEvent be = (BodyEvent) seList.get(1);
        assertEquals("xxx & yyy", be.getText());
    }

    @Test
    public void test3() throws Exception {
        List<SaxEvent> seList = doTest("inc.xml");
        assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        assertEquals(4, seList.size());

        StartEvent se = (StartEvent) seList.get(1);
        Attributes attr = se.getAttributes();
        assertNotNull(attr);
        assertEquals("1", attr.getValue("increment"));
    }

    @Test
    public void bodyWithSpacesAndQuotes() throws Exception {
        List<SaxEvent> seList = doTest("spacesAndQuotes.xml");
        assertEquals(3, seList.size());
        BodyEvent be = (BodyEvent) seList.get(1);
        assertEquals("[x][x] \"xyz\"%n", be.getText());
    }

}
