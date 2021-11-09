package ch.qos.logback.classic.pattern;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class EnsureExceptionHandlingTest {

    private final PatternLayout pl = new PatternLayout();
    private final LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(this.getClass());

    static final String XTH = "xth";
    static final String XCC = "xcc";

    @Before
    public void setUp() {
        pl.setContext(lc);
        pl.getInstanceConverterMap().put(XTH, XThrowableHandlingConverter.class.getName());
        pl.getInstanceConverterMap().put(XCC, XCompositeConverter.class.getName());
    }

    ILoggingEvent makeLoggingEvent(final String msg, final Exception ex) {
        return new LoggingEvent(EnsureExceptionHandlingTest.class.getName(), logger,
                        Level.INFO, msg, ex, null);
    }

    @Test
    public void smoke() {
        pl.setPattern("%m %"+XTH+")");
        pl.start();
        final ILoggingEvent le = makeLoggingEvent("assert", null);
        pl.doLayout(le);
    }

    @Test
    public void withinComposite() {
        pl.setPattern("%m %"+XCC+"(%"+XTH+")");
        pl.start();
        final ILoggingEvent le = makeLoggingEvent("assert", null);
        pl.doLayout(le);
    }

}
