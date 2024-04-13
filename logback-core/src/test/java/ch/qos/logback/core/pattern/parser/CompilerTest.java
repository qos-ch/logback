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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.Converter123;
import ch.qos.logback.core.pattern.ConverterHello;
import ch.qos.logback.core.status.testUtil.StatusChecker;
//import ch.qos.logback.core.util.StatusPrinter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompilerTest {

    Context context = new ContextBase();

    String write(final Converter<Object> head, Object event) {
        StringBuilder buf = new StringBuilder();
        Converter<Object> c = head;
        while (c != null) {
            c.write(buf, event);
            c = c.getNext();
        }
        return buf.toString();
    }

    /**
     * Choose and invoke one of the p.compile methods depending on whether
     * the converterSupplierMap is null
     */
    Converter<Object> compile(Parser<Object> p, Node t,
            Map<String, String> converterMap, 
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) {
        if (converterSupplierMap == null) {
            // null supplier map so call the 2 arg method
            return p.compile(t, converterMap);
        } else {
            // non-null supplier map so call the 3 arg method
            return p.compile(t, converterMap, converterSupplierMap);
        }
    }

    /**
     * ParameterizedTest source to run the test with different combinations
     * of arguments
     */
    protected static Stream<Arguments> converterMapArgs() {
        // converters whose value is class names
        Map<String, String> converterMap = new HashMap<String, String>();
        converterMap.put("OTT", Converter123.class.getName());
        converterMap.put("hello", ConverterHello.class.getName());
        converterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        // only the default converters whose value is class names 
        Map<String, String> onlyDefaultConverterMap = new HashMap<String, String>();
        onlyDefaultConverterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        // converters whose value is supplier functions
        Map<String, Supplier<Converter<Object>>> converterSupplierMap = new HashMap<String, Supplier<Converter<Object>>>();
        converterSupplierMap.put("OTT", Converter123::new);
        converterSupplierMap.put("hello", ConverterHello::new);

        return Stream.of(
                    Arguments.of("converterMap, null converterSupplierMap", converterMap, null),
                    Arguments.of("converterMap, empty converterSupplierMap", converterMap, Collections.emptyMap()),
                    Arguments.of("onlyDefaultConverterMap, converterSupplierMap", onlyDefaultConverterMap, converterSupplierMap)
                );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testLiteral(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        Parser<Object> p = new Parser<Object>("hello");
        Node t = p.parse();
        Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
        String result = write(head, new Object());
        assertEquals("hello", result);
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testBasic(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        {
            Parser<Object> p = new Parser<Object>("abc %hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hello", result);
        }
        {
            Parser<Object> p = new Parser<Object>("abc %hello %OTT");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hello 123", result);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void converterStart(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        {
            Parser<Object> p = new Parser<Object>("abc %hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hello", result);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testFormat(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        {
            Parser<Object> p = new Parser<Object>("abc %7hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc   Hello", result);
        }

        {
            Parser<Object> p = new Parser<Object>("abc %-7hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hello  ", result);
        }

        {
            Parser<Object> p = new Parser<Object>("abc %.3hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc llo", result);
        }

        {
            Parser<Object> p = new Parser<Object>("abc %.-3hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hel", result);
        }

        {
            Parser<Object> p = new Parser<Object>("abc %4.5OTT");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc  123", result);
        }
        {
            Parser<Object> p = new Parser<Object>("abc %-4.5OTT");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc 123 ", result);
        }
        {
            Parser<Object> p = new Parser<Object>("abc %3.4hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc ello", result);
        }
        {
            Parser<Object> p = new Parser<Object>("abc %-3.-4hello");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("abc Hell", result);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testComposite(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        // {
        // Parser<Object> p = new Parser<Object>("%(ABC)");
        // p.setContext(context);
        // Node t = p.parse();
        // Converter<Object> head = p.compile(t, converterMap, converterSupplierMap);
        // String result = write(head, new Object());
        // assertEquals("ABC", result);
        // }
        {
            Context c = new ContextBase();
            Parser<Object> p = new Parser<Object>("%(ABC %hello)");
            p.setContext(c);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            // StatusPrinter.print(c);
            assertEquals("ABC Hello", result);
        }
        {
            Parser<Object> p = new Parser<Object>("%(ABC %hello)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("ABC Hello", result);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testCompositeFormatting(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        {
            Parser<Object> p = new Parser<Object>("xyz %4.10(ABC)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz  ABC", result);
        }

        {
            Parser<Object> p = new Parser<Object>("xyz %-4.10(ABC)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz ABC ", result);
        }

        {
            Parser<Object> p = new Parser<Object>("xyz %.2(ABC %hello)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz lo", result);
        }

        {
            Parser<Object> p = new Parser<Object>("xyz %.-2(ABC)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz AB", result);
        }

        {
            Parser<Object> p = new Parser<Object>("xyz %30.30(ABC %20hello)");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz       ABC                Hello", result);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testUnknownWord(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        Parser<Object> p = new Parser<Object>("%unknown");
        p.setContext(context);
        Node t = p.parse();
        compile(p, t, converterMap, converterSupplierMap);
        StatusChecker checker = new StatusChecker(context.getStatusManager());
        checker.assertContainsMatch("\\[unknown] is not a valid conversion word");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("converterMapArgs")
    public void testWithNopEscape(String label, Map<String, String> converterMap,
            Map<String, Supplier<Converter<Object>>> converterSupplierMap) throws Exception {
        {
            Parser<Object> p = new Parser<Object>("xyz %hello\\_world");
            p.setContext(context);
            Node t = p.parse();
            Converter<Object> head = compile(p, t, converterMap, converterSupplierMap);
            String result = write(head, new Object());
            assertEquals("xyz Helloworld", result);
        }
    }

}
