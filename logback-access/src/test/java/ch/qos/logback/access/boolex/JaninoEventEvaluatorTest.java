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
package ch.qos.logback.access.boolex;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EvaluationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JaninoEventEvaluatorTest {

    final String expectedURL1 = "testUrl1";
    final String expectedURL2 = "testUrl2";
    Context context = new ContextBase();
    JaninoEventEvaluator evaluator;
    DummyRequest request;
    DummyResponse response;
    DummyServerAdapter serverAdapter;

    @Before
    public void setUp() throws Exception {
        evaluator = new JaninoEventEvaluator();
        evaluator.setContext(context);
        request = new DummyRequest();
        response = new DummyResponse();
        serverAdapter = new DummyServerAdapter(request, response);
    }

    @Test
    public void smoke() throws EvaluationException {
        evaluator.setExpression("event.getProtocol().equals(\"testProtocol\")");
        evaluator.start();
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        assertTrue(evaluator.evaluate(ae));
    }

    @Test
    public void block() throws EvaluationException {
        evaluator.setExpression("String protocol = event.getProtocol();" + "return protocol.equals(\"testProtocol\");");
        evaluator.start();
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        assertTrue(evaluator.evaluate(ae));
    }

    @Test
    public void invalidExpression() throws EvaluationException {
        evaluator.setExpression("return true");
        evaluator.start();
        IAccessEvent ae = new AccessEvent(request, response, serverAdapter);
        try {
            evaluator.evaluate(ae);
            fail("Was expecting an exception");
        } catch (IllegalStateException e) {
        }
    }
}
