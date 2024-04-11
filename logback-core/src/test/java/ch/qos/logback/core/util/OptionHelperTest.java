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

import static ch.qos.logback.core.subst.NodeToStringTransformer.CIRCULAR_VARIABLE_REFERENCE_DETECTED;
import static ch.qos.logback.core.subst.Parser.EXPECTING_DATA_AFTER_LEFT_ACCOLADE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ScanException;
import org.junit.jupiter.api.Timeout;

public class OptionHelperTest {

    String text = "Testing ${v1} variable substitution ${v2}";
    String expected = "Testing if variable substitution works";
    Context context = new ContextBase();
    Map<String, String> secondaryMap;

    int diff = RandomUtil.getPositiveInt();

    @BeforeEach
    public void setUp() throws Exception {
        secondaryMap = new HashMap<String, String>();
    }

    @Test
    public void testLiteral() throws ScanException {
        String noSubst = "hello world";
        String result = OptionHelper.substVars(noSubst, context);
        assertEquals(noSubst, result);
    }

    @Test
    public void testUndefinedValues() throws ScanException {
        String withUndefinedValues = "${axyz}";

        String result = OptionHelper.substVars(withUndefinedValues, context);
        assertEquals("axyz" + OptionHelper._IS_UNDEFINED, result);
    }

    @Test
    public void testSubstVarsVariableNotClosed() throws ScanException {
        String noSubst = "testing if ${v1 works";

        try {
            @SuppressWarnings("unused")
            String result = OptionHelper.substVars(noSubst, context);
            fail();
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testSubstVarsContextOnly() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "works");

        String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsSystemProperties() throws ScanException {
        System.setProperty("v1", "if");
        System.setProperty("v2", "works");

        String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);

        System.clearProperty("v1");
        System.clearProperty("v2");
    }

    @Test
    public void testSubstVarsWithDefault() throws ScanException {
        context.putProperty("v1", "if");
        String textWithDefault = "Testing ${v1} variable substitution ${v2:-toto}";
        String resultWithDefault = "Testing if variable substitution toto";

        String result = OptionHelper.substVars(textWithDefault, context);
        assertEquals(resultWithDefault, result);
    }

    @Test
    public void testSubstVarsRecursive() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "works");

        String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsTwoLevelsDeep() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "${v4}");
        context.putProperty("v4", "works");

        String result = OptionHelper.substVars(text, context);
        assertEquals(expected, result);
    }

    @Test
    public void testSubstVarsTwoLevelsWithDefault() throws ScanException {
        // Example input taken from LOGBCK-943 bug report
        context.putProperty("APP_NAME", "LOGBACK");
        context.putProperty("ARCHIVE_SUFFIX", "archive.log");
        context.putProperty("LOG_HOME", "${logfilepath.default:-logs}");
        context.putProperty("ARCHIVE_PATH", "${LOG_HOME}/archive/${APP_NAME}");

        String result = OptionHelper.substVars("${ARCHIVE_PATH}_trace_${ARCHIVE_SUFFIX}", context);
        assertEquals("logs/archive/LOGBACK_trace_archive.log", result);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void stubstVarsShouldNotGoIntoInfiniteLoop() throws ScanException {
        context.putProperty("v1", "if");
        context.putProperty("v2", "${v3}");
        context.putProperty("v3", "${v4}");
        context.putProperty("v4", "${v2}c");

        Exception e = assertThrows(Exception.class, () -> {
            OptionHelper.substVars(text, context);
        });
        String expectedMessage =  CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${v2} --> ${v3} --> ${v4} --> ${v2}]";
        assertEquals(expectedMessage, e.getMessage());
    }
    
    @Test
    public void nonCircularGraphShouldWork() throws ScanException {
        context.putProperty("A", "${B} and ${C}");
        context.putProperty("B", "${B1}");
        context.putProperty("B1", "B1-value");
        context.putProperty("C", "${C1} and ${B}");
        context.putProperty("C1", "C1-value");

        String result = OptionHelper.substVars("${A}", context);
        assertEquals("B1-value and C1-value and B1-value", result);
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void detectCircularReferences0() throws ScanException {
        context.putProperty("A", "${A}");
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
           OptionHelper.substVars("${A}", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${A} --> ${A}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void detectCircularReferences1() throws ScanException {
        context.putProperty("A", "${A}a");

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("${A}", context);
        });
        
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${A} --> ${A}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void detectCircularReferences2() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
           OptionHelper.substVars("${A}", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${A} --> ${B} --> ${C} --> ${A}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=46755
    @Test
    public void recursionErrorWithNullLiteralPayload() throws ScanException {

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
           OptionHelper.substVars("abc${AA$AA${}}}xyz", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${AA} --> ${}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=46892
    @Test
    public void leftAccoladeFollowedByDefaultStateWithNoLiteral() throws ScanException {
        Exception e = assertThrows(ScanException.class, () -> {
            OptionHelper.substVars("x{:-a}", context);
        });
        String expectedMessage = EXPECTING_DATA_AFTER_LEFT_ACCOLADE;
        assertEquals(expectedMessage, e.getMessage());
    }

    // https://bugs.chromium.org/p/oss-fuzz/issues/detail?id=46966
    @Test
    public void nestedEmptyVariables() throws ScanException {

        Exception e = assertThrows(Exception.class, () -> {
            OptionHelper.substVars("${${${}}}", context);
        });
        String expectedMessage =  CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${ ?  ? } --> ${ ? } --> ${}]";
        assertEquals(expectedMessage, e.getMessage());
    }
    
    
    
    @Test
    public void detectCircularReferencesInDefault() throws ScanException {
        context.putProperty("A", "${B:-${A}}");
     

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("${A}", context);
        });

        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${A} --> ${B} --> ${A}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void detectCircularReferences3() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("${B} ", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED + "${B} --> ${C} --> ${A} --> ${B}]";
        assertEquals(expectedMessage, e.getMessage());

    }

    @Test
    @Timeout(value = 1, unit = SECONDS)
    public void detectCircularReferences4() throws ScanException {
        context.putProperty("A", "${B}");
        context.putProperty("B", "${C}");
        context.putProperty("C", "${A}");

        
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("${C} and ${A}", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${C} --> ${A} --> ${B} --> ${C}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    public void detectCircularReferences5() throws ScanException {
        context.putProperty("A", "${B} and ${C}");
        context.putProperty("B", "${B1}");
        context.putProperty("B1", "B1-value");
        context.putProperty("C", "${C1}");
        context.putProperty("C1", "here's the loop: ${A}");

        
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("${A}", context);
        });
        String expectedMessage = CIRCULAR_VARIABLE_REFERENCE_DETECTED+"${A} --> ${C} --> ${C1} --> ${A}]";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    public void defaultValueReferencingAVariable() throws ScanException {
        context.putProperty("v1", "k1");
        String result = OptionHelper.substVars("${undef:-${v1}}", context);
        assertEquals("k1", result);
    }

    @Test
    public void jackrabbit_standalone() throws ScanException {
        String r = OptionHelper.substVars("${jackrabbit.log:-${repo:-jackrabbit}/log/jackrabbit.log}", context);
        assertEquals("jackrabbit/log/jackrabbit.log", r);
    }

    @Test
    public void emptyVariableIsAccepted() throws JoranException, ScanException {
        String varName = "var"+diff;
        context.putProperty(varName, "");
        String r = OptionHelper.substVars("x ${"+varName+"} b", context);
        assertEquals("x  b", r);
    }

    // https://jira.qos.ch/browse/LOGBACK-1012
    // conflicts with the idea that variables assigned the empty string are valid
    @Disabled
    @Test
    public void defaultExpansionForEmptyVariables() throws JoranException, ScanException {
        String varName = "var"+diff;
        context.putProperty(varName, "");

        String r = OptionHelper.substVars("x ${"+varName+":-def} b", context);
        assertEquals("x def b", r);
    }

    @Test
    public void emptyDefault() throws ScanException {
        String r = OptionHelper.substVars("a${undefinedX:-}b", context);
        assertEquals("ab", r);
    }

    @Test
    public void openBraceAsLastCharacter() throws JoranException, ScanException {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            OptionHelper.substVars("a{a{", context);
        });
        String expectedMessage = "All tokens consumed but was expecting \"}\"";
        assertEquals(expectedMessage, e.getMessage());
    }

    
    @Test
    public void trailingColon_LOGBACK_1140() throws ScanException {
        String prefix = "c:";
        String suffix = "/tmp";
        context.putProperty("var", prefix);
        String r = OptionHelper.substVars("${var}" + suffix, context);
        assertEquals(prefix + suffix, r);
    }




    @Test
    public void curlyBraces_LOGBACK_1101() throws ScanException {
        {
            String input = "foo{bar}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            String input = "{foo{\"bar\"}}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            String input = "a:{y}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            String input = "{world:{yay}}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            String input = "{hello:{world:yay}}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
        {
            String input = "{\"hello\":{\"world\":\"yay\"}}";
            String r = OptionHelper.substVars(input, context);
            assertEquals(input, r);
        }
    }
}
