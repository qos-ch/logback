package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.ILoggingEvent;

import static org.junit.jupiter.api.Assertions.assertNull;

public class XThrowableHandlingConverter extends ThrowableHandlingConverter {

    void assertNoNext() {
        assertNull(this.getNext(), "has next");
    }

    @Override
    public String convert(ILoggingEvent event) {
        if (event.getMessage().contains("assert"))
            assertNoNext();
        return "";
    }

}
