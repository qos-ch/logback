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
package ch.qos.logback.classic.boolex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnMarkerEvaluatorTest {

    LoggerContext lc = new LoggerContext();
    LoggingEvent event = makeEvent();
    OnMarkerEvaluator evaluator = new OnMarkerEvaluator();

    @BeforeEach
    public void before() {
        evaluator.setContext(lc);
    }

    @Test
    public void smoke() throws EvaluationException {
        evaluator.addMarker("M");
        evaluator.start();

        event.addMarker(MarkerFactory.getMarker("M"));
        assertTrue(evaluator.evaluate(event));
    }

    @Test
    public void nullMarkerInEvent() throws EvaluationException {
        evaluator.addMarker("M");
        evaluator.start();
        assertFalse(evaluator.evaluate(event));
    }

    @Test
    public void nullMarkerInEvaluator() throws EvaluationException {
        evaluator.addMarker("M");
        evaluator.start();
        assertFalse(evaluator.evaluate(event));
    }

    LoggingEvent makeEvent() {
        return new LoggingEvent("x", lc.getLogger("x"), Level.DEBUG, "msg", null, null);
    }
}
