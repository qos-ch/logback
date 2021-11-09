package ch.qos.logback.core.pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.ScanException;

// inspired by ch.qos.logback.core.pattern.parser.CompilerTest
public class ConverterUtilTest {

    Map<String, String> converterMap = new HashMap<>();
    Context context = new ContextBase();

    @Before
    public void setUp() {
        converterMap.put("OTT", Converter123.class.getName());
        converterMap.put("hello", ConverterHello.class.getName());
        converterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);
    }

    @Test
    public void contextAndStartTest() throws ScanException {
        testContextAndStart("hi %hello");
        testContextAndStart("hi %(%hello)");
        testContextAndStart("hi %(abc %(%hello))");

    }

    private void testContextAndStart(final String pattern) throws ScanException {
        final Parser<Object> p = new Parser<>(pattern);
        p.setContext(context);
        final Node t = p.parse();
        final Converter<Object> head = p.compile(t, converterMap);
        ConverterUtil.setContextForConverters(context, head);
        checkContext(head);

        ConverterUtil.startConverters(head);
        checkStart(head);
    }

    private void checkStart(final Converter<Object> head) {
        Converter<Object> c = head;
        while (c != null) {
            if (c instanceof LifeCycle) {
                final LifeCycle ca = (LifeCycle) c;
                assertTrue(ca.isStarted());
            }
            if (c instanceof CompositeConverter) {
                final CompositeConverter<Object> cc = (CompositeConverter<Object>) c;
                final Converter<Object> childConverter = cc.childConverter;
                checkStart(childConverter);
            }
            c = c.getNext();
        }

    }

    void checkContext(final Converter<Object> head) {
        Converter<Object> c = head;
        while (c != null) {
            if (c instanceof ContextAware) {
                final ContextAware ca = (ContextAware) c;
                assertNotNull(ca.getContext());
            }
            if (c instanceof CompositeConverter) {
                final CompositeConverter<Object> cc = (CompositeConverter<Object>) c;
                final Converter<Object> childConverter = cc.childConverter;
                checkContext(childConverter);
            }
            c = c.getNext();
        }
    }

}
