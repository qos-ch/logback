package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

import static org.junit.jupiter.api.Assertions.assertNull;

public class XCompositeConverter extends CompositeConverter<ILoggingEvent> {

    void assertNoNext() {
        assertNull( this.getNext(), "converter instance has next element");
    }

    @Override
    protected String transform(ILoggingEvent event, String in) {
        if (event.getMessage().contains("assert"))
            assertNoNext();
        return "";
    }

}
