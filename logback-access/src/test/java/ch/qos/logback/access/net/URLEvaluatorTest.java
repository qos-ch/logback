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
package ch.qos.logback.access.net;

import static org.junit.Assert.*;

import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EvaluationException;

public class URLEvaluatorTest {

    final String expectedURL1 = "testUrl1";
    final String expectedURL2 = "testUrl2";
    Context context = new ContextBase();
    URLEvaluator evaluator;
    DummyRequest request;
    DummyResponse response;
    DummyServerAdapter serverAdapter;

    @Before
    public void setUp() throws Exception {
        evaluator = new URLEvaluator();
        evaluator.setContext(context);
        evaluator.addURL(expectedURL1);
        evaluator.start();
        request = new DummyRequest();
        response = new DummyResponse();
        serverAdapter = new DummyServerAdapter(request, response);
    }

    @After
    public void tearDown() throws Exception {
        evaluator.stop();
        evaluator = null;
        request = null;
        response = null;
        serverAdapter = null;
        context = null;
    }

    @Test
    public void testExpectFalse() throws EvaluationException {
        request.setRequestUri("test");
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        assertFalse(evaluator.evaluate(ae));
    }

    @Test
    public void testExpectTrue() throws EvaluationException {
        request.setRequestUri(expectedURL1);
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        assertTrue(evaluator.evaluate(ae));
    }

    @Test
    public void testExpectTrueMultiple() throws EvaluationException {
        evaluator.addURL(expectedURL2);
        request.setRequestUri(expectedURL2);
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        assertTrue(evaluator.evaluate(ae));
    }
}
