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
package ch.qos.logback.core.pattern.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.parser.test.AbstractPatternLayoutBaseTest;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.StatusChecker;

public class SamplePatternLayoutTest extends AbstractPatternLayoutBaseTest<Object> {

    Context context = new ContextBase();
    StatusChecker checker = new StatusChecker(context);

    @Override
    public PatternLayoutBase<Object> getPatternLayoutBase() {
        return new SamplePatternLayout<>();
    }

    @Override
    public Object getEventObject() {
        return new Object();
    }

    @Test
    public void testOK() {
        final PatternLayoutBase<Object> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern("x%OTT");
        plb.start();
        final String s = plb.doLayout(new Object());
        // System.out.println(s);

        // StatusManager sm = context.getStatusManager();
        // StatusPrinter.print(sm);
        assertEquals("x123", s);
    }

    @Test
    public void testEscapeClosingParentheses() {
        final PatternLayoutBase<Object> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern("x(%OTT\\)y");
        plb.start();
        final String s = plb.doLayout(new Object());
        assertEquals("x(123)y", s);
    }

    @Test
    public void testEscapeBothParentheses() {
        final PatternLayoutBase<Object> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern("x\\(%OTT\\)y");
        plb.start();
        final String s = plb.doLayout(new Object());
        assertEquals("x(123)y", s);
    }

    @Test
    public void testPercentAsLiteral() {
        final PatternLayoutBase<Object> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern("hello \\% world");
        plb.start();
        final String s = plb.doLayout(new Object());
        assertEquals("hello % world", s);
    }


    @Test
    public void noClosingCurlyBrace() {
        final PatternLayoutBase<Object> plb = getPatternLayoutBase();
        plb.setContext(context);
        plb.setPattern("%x %hello{asd");
        plb.start();

        checker.assertContainsMatch(Status.ERROR, "Failed to parse pattern");
    }

    @Override
    public Context getContext() {
        return context;
    }
}
