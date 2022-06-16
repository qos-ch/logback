package ch.qos.logback.classic.jul;

public class MinSlf4jLevel extends java.util.logging.Level {
    private MinSlf4jLevel(String name, int value) {
        super(name, value);
    }

    /**
     * A JUL Level with the smallest intValue that jul-to-slf4j
     * converts to a SLF4J DEBUG.
     */
    public static MinSlf4jLevel MIN_DEBUG = new MinSlf4jLevel(
        "MIN_DEBUG", java.util.logging.Level.FINEST.intValue() + 1);
    /**
     * A JUL Level with the smallest intValue that jul-to-slf4j
     * converts to a SLF4J INFO.
     */
    public static MinSlf4jLevel MIN_INFO = new MinSlf4jLevel(
        "MIN_INFO", java.util.logging.Level.FINE.intValue() + 1);
    /**
     * A JUL Level with the smallest intValue that jul-to-slf4j
     * converts to a SLF4J WARN.
     */
    public static MinSlf4jLevel MIN_WARN = new MinSlf4jLevel(
        "MIN_WARN", java.util.logging.Level.INFO.intValue() + 1);
    /**
     * A JUL Level with the smallest intValue that jul-to-slf4j
     * converts to a SLF4J ERROR.
     */
    public static MinSlf4jLevel MIN_ERROR = new MinSlf4jLevel(
        "MIN_ERROR", java.util.logging.Level.WARNING.intValue() + 1);
}
