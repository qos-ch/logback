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
package ch.qos.logback.core.pattern.parser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.ExceptionalConverter;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.testUtil.StatusChecker;

abstract public class AbstractPatternLayoutBaseTest<E> {

    abstract public PatternLayoutBase<E> getPatternLayoutBase();

    abstract public E getEventObject();

    abstract public Context getContext();

    @Test
    public void testUnStarted() {
        final PatternLayoutBase<E> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        final String s = plb.doLayout(getEventObject());
        assertEquals("", s);
    }

    /**
     * This test checks that the pattern layout implementation starts its
     * converters. ExceptionalConverter throws an exception if it's convert
     * method is called before being started.
     */
    @Test
    public void testConverterStart() {
        final PatternLayoutBase<E> plb = getPatternLayoutBase();
        plb.setContext(getContext());
        plb.getInstanceConverterMap().put("EX", ExceptionalConverter.class.getName());
        plb.setPattern("%EX");
        plb.start();
        final String result = plb.doLayout(getEventObject());
        assertFalse(result.contains("%PARSER_ERROR_EX"));
    }

    @Test
    public void testStarted() {
        final PatternLayoutBase<E> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        final String s = plb.doLayout(getEventObject());
        assertEquals("", s);
    }

    @Test
    public void testNullPattern() {
        // System.out.println("testNullPattern");
        final PatternLayoutBase<E> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern(null);
        plb.start();
        final String s = plb.doLayout(getEventObject());
        assertEquals("", s);
        final StatusChecker checker = new StatusChecker(context.getStatusManager());
        checker.assertContainsMatch("Empty or null pattern.");
    }

    @Test
    public void testEmptyPattern() {
        // System.out.println("testNullPattern");
        final PatternLayoutBase<E> plb = getPatternLayoutBase();
        final Context context = new ContextBase();
        plb.setContext(context);
        plb.setPattern("");
        plb.start();
        final String s = plb.doLayout(getEventObject());
        assertEquals("", s);
        final StatusChecker checker = new StatusChecker(context.getStatusManager());
        checker.assertContainsMatch("Empty or null pattern.");
    }

}
