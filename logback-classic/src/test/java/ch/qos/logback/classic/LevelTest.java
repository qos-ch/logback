package ch.qos.logback.classic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelTest {

    @Test
    public void smoke() {
        assertEquals(Level.TRACE, Level.toLevel("TRACE"));
        assertEquals(Level.DEBUG, Level.toLevel("DEBUG"));
        assertEquals(Level.INFO, Level.toLevel("INFO"));
        assertEquals(Level.WARN, Level.toLevel("WARN"));
        assertEquals(Level.ERROR, Level.toLevel("ERROR"));
    }

    @Test
    public void withSpaceSuffix() {
        assertEquals(Level.INFO, Level.toLevel("INFO "));
    }
}
