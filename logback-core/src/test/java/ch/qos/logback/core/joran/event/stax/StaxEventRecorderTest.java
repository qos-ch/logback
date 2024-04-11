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
package ch.qos.logback.core.joran.event.stax;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.stream.events.Attribute;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaxEventRecorderTest {

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);

    public List<StaxEvent> doTest(String filename) throws Exception {
        StaxEventRecorder recorder = new StaxEventRecorder(context);
        FileInputStream fis = new FileInputStream(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/" + filename);
        recorder.recordEvents(fis);
        return recorder.getEventList();
    }

    public void dump(List<StaxEvent> seList) {
        for (StaxEvent se : seList) {
            System.out.println(se);
        }
    }

    @Test
    public void testParsingOfXMLWithAttributesAndBodyText() throws Exception {
        List<StaxEvent> seList = doTest("event1.xml");
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        Assertions.assertEquals(11, seList.size());
        Assertions.assertEquals("test", seList.get(0).getName());
        Assertions.assertEquals("badBegin", seList.get(1).getName());
        StartEvent startEvent = (StartEvent) seList.get(7);
        Assertions.assertEquals("John Doe", startEvent.getAttributeByName("name").getValue());
        Assertions.assertEquals("XXX&", ((BodyEvent) seList.get(8)).getText());
    }

    @Test
    public void testProcessingOfTextWithEntityCharacters() throws Exception {
        List<StaxEvent> seList = doTest("ampEvent.xml");
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        // dump(seList);
        Assertions.assertEquals(3, seList.size());

        BodyEvent be = (BodyEvent) seList.get(1);
        Assertions.assertEquals("xxx & yyy", be.getText());
    }

    @Test
    public void testAttributeProcessing() throws Exception {
        List<StaxEvent> seList = doTest("inc.xml");
        Assertions.assertTrue(statusChecker.getHighestLevel(0) == Status.INFO);
        Assertions.assertEquals(4, seList.size());
        StartEvent se = (StartEvent) seList.get(1);
        Attribute attr = se.getAttributeByName("increment");
        Assertions.assertNotNull(attr);
        Assertions.assertEquals("1", attr.getValue());
    }

    @Test
    public void bodyWithSpacesAndQuotes() throws Exception {
        List<StaxEvent> seList = doTest("spacesAndQuotes.xml");
        Assertions.assertEquals(3, seList.size());
        BodyEvent be = (BodyEvent) seList.get(1);
        Assertions.assertEquals("[x][x] \"xyz\"%n", be.getText());
    }
}
