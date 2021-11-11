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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ScanException;

public class OptionHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    String text = "Testing ${v1} variable substitution ${v2}";
    String expected = "Testing if variable substitution works";
    Context context = new ContextBase();
    Map<String, String> secondaryMap;

    @Before
    public void setUp() throws Exception {
        secondaryMap = new HashMap<>();
    }

    @Test
    public void testLiteral() throws ScanException {
        final String noSubst = "hello world";
        final String result = OptionHelper.substVars(noSubst, context);
        assertEquals(noSubst, result);
    }

    @Test
    public void testUndefinedValues() throws ScanException {
        final String withUndefinedValues = "${axyz}";

        final String result = OptionHelper.substVars(withUndefinedValues, context);
        assertEquals("axyz" + OptionHelper._IS_UNDEFINED, result);
    }

    @Test
    public void testSubstVarsVariableNotClosed() throws ScanException {
        final String noSubst = "testing if ${v1 works";

        try {
            @SuppressWarnings("unused")
            final String result = OptionHelper.substVars(noSubst, context);
            fail();
        } catch (final IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testSubstVarsContextOnly() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "works");

        final String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsSystemProperties() throws ScanException {
        System.setProperty("v1", "if");
        System.setProperty("v2", "works");

        final String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);

        System.clearProperty("v1");
        System.clearProperty("v2");
    }

    @Test
    public void testSubstVarsWithDefault() throws ScanException {
        context.putProperty("v1", "if");
        final String textWithDefault = "Testing ${v1} variable substitution ${v2:-toto}";
        final String resultWithDefault = "Testing if variable substitution toto";

        final String result = OptionHelper.substVars(textWithDefault, context);
        assertEquals(resultWithDefault, result);
    }

    @Test
    public void testSubstVarsRecursive() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "works");

        final String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsTwoLevelsDeep() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "${v4}");
        context.putProperty("v4", "works");

        final String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsTwoLevelsWithDefault() throws ScanException {
        // Example input taken from LOGBCK-943 bug report
        context.putProperty("APP_NAME", "LOGBACK");
        context.putProperty("ARCHIVE_SUFFIX", "archive.log");
        context.putProperty("LOG_HOME", "${logfilepath.default:-logs}");
        context.putProperty("ARCHIVE_PATH", "${LOG_HOME}/archive/${APP_NAME}");

        final String result = OptionHelper.substVars("${ARCHIVE_PATH}_trace_${ARCHIVE_SUFFIX}", context);
        assertEquals("logs/archive/LOGBACK_trace_archive.log", result);
    }

    @Test(timeout = 1000)
    public void stubstVarsShouldNotGoIntoInfiniteLoop() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "${v4}");
        context.putProperty("v4", "${v2}c");

        expectedException.expect(Exception.class);
        OptionHelper.substVars(text, context);
    }

    @Test
    public void nonCircularGraphShouldWork() throws ScanException {
        context.putProperty("A", "${B} and ${C}");
        context.putProperty("B", "${B1}");
        context.putProperty("B1", "B1-value");
        context.putProperty("C", "${C1} and ${B}");
        context.putProperty("C1", "C1-value");

        final String result = OptionHelper.substVars("${A}", context);
        assertEquals("B1-value and C1-value and B1-value", result);
    }

    @Test(timeout = 1000)
    public void detectCircularReferences0() throws ScanException {
        context.putProperty("A", "${A}");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${A} --> ${A}]");
        OptionHelper.substVars("${A}", context);
    }

    @Test(timeout = 1000)
    public void detectCircularReferences1() throws ScanException {
        context.putProperty("A", "${A}a");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${A} --> ${A}]");
        OptionHelper.substVars("${A}", context);
    }

    @Test(timeout = 1000)
    public void detectCircularReferences2() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${A} --> ${B} --> ${C} --> ${A}]");
        OptionHelper.substVars("${A}", context);
    }

    @Test
    public void detectCircularReferencesInDefault() throws ScanException {
        context.putProperty("A", "${B:-${A}}");
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${A} --> ${B} --> ${A}]");
        OptionHelper.substVars("${A}", context);
    }

    @Test(timeout = 1000)
    public void detectCircularReferences3() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${B} --> ${C} --> ${A} --> ${B}]");
        OptionHelper.substVars("${B} ", context);
    }

    @Test(timeout = 1000)
    public void detectCircularReferences4() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${C} --> ${A} --> ${B} --> ${C}]");
        OptionHelper.substVars("${C} and ${A}", context);
    }

    @Test
    public void detectCircularReferences5() throws ScanException {
        context.putProperty("A", "${B} and ${C}");
        context.putProperty("B", "${B1}");
        context.putProperty("B1", "B1-value");
        context.putProperty("C", "${C1}");
        context.putProperty("C1", "here's the loop: ${A}");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Circular variable reference detected while parsing input [${A} --> ${C} --> ${C1} --> ${A}]");
        final String result = OptionHelper.substVars("${A}", context);
        System.err.println(result);
    }

    @Test
    public void defaultValueReferencingAVariable() throws ScanException {
        context.putProperty("v1", "k1");
        final String result = OptionHelper.substVars("${undef:-${v1}}", context);
        assertEquals("k1", result);
    }

    @Test
    public void jackrabbit_standalone() throws ScanException {
        final String r = OptionHelper.substVars("${jackrabbit.log:-${repo:-jackrabbit}/log/jackrabbit.log}", context);
        assertEquals("jackrabbit/log/jackrabbit.log", r);
    }

    @Test
    public void doesNotThrowNullPointerExceptionForEmptyVariable() throws JoranException, ScanException {
        context.putProperty("var", "");
        OptionHelper.substVars("${var}", context);
    }

    @Test
    public void trailingColon_LOGBACK_1140() throws ScanException {
        final String prefix = "c:";
        final String suffix = "/tmp";
        context.putProperty("var", prefix);
        final String r = OptionHelper.substVars("${var}" + suffix, context);
        assertEquals(prefix + suffix, r);
    }

    @Test
    public void curlyBraces_LOGBACK_1101() throws ScanException {
        {
            final String input = "foo{bar}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            final String input = "{foo{\"bar\"}}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            final String input = "a:{y}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            final String input = "{world:{yay}}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            final String input = "{hello:{world:yay}}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            final String input = "{\"hello\":{\"world\":\"yay\"}}";
            final String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
    }
}
