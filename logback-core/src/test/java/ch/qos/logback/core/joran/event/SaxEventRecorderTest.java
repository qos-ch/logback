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

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xml.sax.Attributes;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.status.testUtil.StatusChecker;

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
    public void testEvent1() throws Exception {
        System.out.println("test1");
        List<SaxEvent> seList = doTest("event1.xml");
        StatusPrinter.print(context);
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        Assertions.assertEquals(11, seList.size());
    }

    @Test()
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)  // timeout in case attack is not prevented
    public void testEventSSRF() throws Exception {
        try {
            List<SaxEvent> seList = doTest("event-ssrf.xml");
            Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.WARN);
            statusChecker.assertContainsMatch(Status.WARN, "Document Type Declaration");
            Assertions.assertEquals(11, seList.size());
        } finally {
            StatusPrinter.print(context);
        }
    }

    @Test
    public void testEventAmp() throws Exception {
        List<SaxEvent> seList = doTest("ampEvent.xml");
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        Assertions.assertEquals(3, seList.size());

        BodyEvent be = (BodyEvent) seList.get(1);
        Assertions.assertEquals("xxx & yyy", be.getText());
    }

    @Test
    public void testInc() throws Exception {
        List<SaxEvent> seList = doTest("inc.xml");
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        Assertions.assertEquals(4, seList.size());

        StartEvent se = (StartEvent) seList.get(1);
        Attributes attr = se.getAttributes();
        Assertions.assertNotNull(attr);
        Assertions.assertEquals("1", attr.getValue("increment"));
    }

    @Test
    public void bodyWithSpacesAndQuotes() throws Exception {
        List<SaxEvent> seList = doTest("spacesAndQuotes.xml");
        Assertions.assertEquals(3, seList.size());
        BodyEvent be = (BodyEvent) seList.get(1);
        Assertions.assertEquals("[x][x] \"xyz\"%n", be.getText());
    }

}
