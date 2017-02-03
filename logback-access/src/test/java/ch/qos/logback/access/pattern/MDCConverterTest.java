package ch.qos.logback.access.pattern;

import ch.qos.logback.access.dummy.DummyAccessEventBuilder;
import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MDCConverterTest {

    private final List<String> options = new ArrayList<String>();

    @Test
    public void testConvert() throws Exception {

        MDCConverter converter = new MDCConverter();
        converter.setOptionList(options);

        MDC.put("a", "0");
        MDC.put("b", "1");

        IAccessEvent ae = DummyAccessEventBuilder.buildNewAccessEvent();

        // full context dump
        {
            options.clear();
            converter.start();

            String res = converter.convert(ae);
            assertTrue(res, res.contains("a=0"));
            assertTrue(res, res.contains("b=1"));

            converter.stop();
        }

        // one key dump
        {
            options.clear();
            options.add("a");
            converter.start();

            String res = converter.convert(ae);
            assertTrue(res, res.contains("0"));
            assertFalse(res, res.contains("1"));

            converter.stop();
        }

        // missing key with default value dump
        {
            options.clear();
            options.add("c");
            converter.start();

            String res = converter.convert(ae);
            assertTrue(res, res.isEmpty());

            converter.stop();
        }

        // missing key with default value dump
        {
            options.clear();
            options.add("c:-X");
            converter.start();

            String res = converter.convert(ae);
            assertTrue(res, res.contains("X"));
            assertFalse(res, res.contains("1"));

            converter.stop();
        }
    }
}