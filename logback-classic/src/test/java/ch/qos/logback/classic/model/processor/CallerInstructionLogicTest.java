/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.model.processor;

import ch.qos.logback.classic.model.processor.CallerInstructionLogic.Instruction;
import ch.qos.logback.core.status.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CallerInstructionLogicTest {

    CallerInstructionLogic logic;

    @BeforeEach
    public void setUp() {
        logic = new CallerInstructionLogic();
    }

    @Test
    public void emptyMapReportsNoContradiction() {
        List<Status> statuses = logic.contradiction(Map.of());

        assertEquals(1, statuses.size());
        assertInfoNoContradiction(statuses.get(0));
    }

    @Test
    public void onlyDirectWantReportsNoContradiction() {
        Map<String, Instruction> map = Map.of("CONSOLE", Instruction.DIRECT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(1, statuses.size());
        assertInfoNoContradiction(statuses.get(0));
    }



    @Test
    public void onlyDoNotWantReportsNoContradiction() {
        Map<String, Instruction> map = Map.of("ASYNC", Instruction.DO_NOT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(1, statuses.size());
        assertInfoNoContradiction(statuses.get(0));
    }

    @Test
    public void preprocessWantWithDirectWantReportsNoContradiction() {
        Map<String, Instruction> map = new LinkedHashMap<>();
        map.put("PRE", Instruction.PREPROCESS_WANT);
        map.put("CONSOLE", Instruction.DIRECT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(1, statuses.size());
        assertInfoNoContradiction(statuses.get(0));
    }


    @Test
    public void lonePreprocessWantReportsContradiction() {
        Map<String, Instruction> map = Map.of("ASYNC", Instruction.PREPROCESS_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(2, statuses.size());
        assertWarnLevel(statuses.get(0));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC"));
        assertSeeUrlWarning(statuses.get(1));
    }

    @Test
    public void doNotWantWithPreprocessWantReportsWarning() {
        Map<String, Instruction> map = new LinkedHashMap<>();
        map.put("ASYNC_0", Instruction.DO_NOT_WANT);
        map.put("ASYNC_1", Instruction.PREPROCESS_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(2, statuses.size());
        assertWarnLevel(statuses.get(0));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC_0"));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC_1"));
        assertSeeUrlWarning(statuses.get(1));
    }

    @Test
    public void doNotWantWithDirectWantReportsWarning() {
        Map<String, Instruction> map = new LinkedHashMap<>();
        map.put("ASYNC", Instruction.DO_NOT_WANT);
        map.put("FILE", Instruction.DIRECT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(2, statuses.size());
        assertWarnLevel(statuses.get(0));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC"));
        assertTrue(statuses.get(0).getMessage().contains("FILE"));
        assertSeeUrlWarning(statuses.get(1));
    }

    @Test
    public void doNotWantWithBothWantInstructionsReportsTwoContradictionWarnings() {
        Map<String, Instruction> map = new LinkedHashMap<>();
        map.put("ASYNC_0", Instruction.DO_NOT_WANT);
        map.put("ASYNC_1", Instruction.PREPROCESS_WANT);
        map.put("FILE", Instruction.DIRECT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(3, statuses.size());
        assertWarnLevel(statuses.get(0));
        assertWarnLevel(statuses.get(1));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC_0"));
        assertTrue(statuses.get(0).getMessage().contains("ASYNC_1"));
        assertTrue(statuses.get(1).getMessage().contains("ASYNC_0"));
        assertTrue(statuses.get(1).getMessage().contains("FILE"));
        assertSeeUrlWarning(statuses.get(2));
    }

    @Test
    public void multipleAppenderNamesAreJoinedInWarningMessage() {
        Map<String, Instruction> map = new LinkedHashMap<>();
        map.put("ASYNC1", Instruction.DO_NOT_WANT);
        map.put("ASYNC2", Instruction.DO_NOT_WANT);
        map.put("CONSOLE", Instruction.DIRECT_WANT);
        map.put("FILE", Instruction.DIRECT_WANT);

        List<Status> statuses = logic.contradiction(map);

        assertEquals(2, statuses.size());
        String msg = statuses.get(0).getMessage();
        assertTrue(msg.contains("ASYNC1"));
        assertTrue(msg.contains("ASYNC2"));
        assertTrue(msg.contains("CONSOLE"));
        assertTrue(msg.contains("FILE"));
        assertSeeUrlWarning(statuses.get(1));
    }

    private static void assertInfoNoContradiction(Status status) {
        assertEquals(Status.INFO, status.getLevel());
        assertTrue(status.getMessage().contains("No contradictions"));
    }

    private static void assertWarnLevel(Status status) {
        assertEquals(Status.WARN, status.getLevel());
    }

    private static void assertSeeUrlWarning(Status status) {
        assertEquals(Status.WARN, status.getLevel());
        assertTrue(status.getMessage().contains(CallerInstructionLogic.CALLER_CONTRADICTION_URL));
    }
}
