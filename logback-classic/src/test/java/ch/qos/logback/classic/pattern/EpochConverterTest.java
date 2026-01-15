package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpochConverterTest {
    EpochConverter ec = new EpochConverter();

    @Test
    public void withDefaultConfiguration() {
        ec.setOptionList(null);
        LoggingEvent le = new LoggingEvent();
        Instant instant = Instant.parse("2026-01-15T10:15:30Z");
        instant = instant.plusMillis(321);
        le.setInstant(instant);

        String result = ec.convert(le);
        assertEquals("1768472130321", result); // includes millis
    }

    @Test
    public void withSecondsConfiguration() {
        ec.setOptionList(Collections.singletonList("seconds"));
        LoggingEvent le = new LoggingEvent();
        Instant instant = Instant.parse("2026-01-15T10:15:30Z");
        instant = instant.plusMillis(321); // millis should be ignored
        le.setInstant(instant);

        String result = ec.convert(le);
        assertEquals("1768472130", result);
    }

    @Test
    public void withUnknownNonsenseConfiguration() {
        ec.setOptionList(Collections.singletonList("nonsense"));
        LoggingEvent le = new LoggingEvent();
        Instant instant = Instant.parse("2026-01-15T10:15:30Z");
        instant = instant.plusMillis(321);
        le.setInstant(instant);

        String result = ec.convert(le);
        assertEquals("1768472130321", result); // includes millis, default behaviour
    }
}