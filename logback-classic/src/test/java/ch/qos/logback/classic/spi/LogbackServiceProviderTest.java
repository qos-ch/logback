package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

public class LogbackServiceProviderTest {

    LogbackServiceProvider provider = new LogbackServiceProvider();

    @Test
    public void testContrxtStart() {
        provider.initialize();
        LoggerContext loggerFactory = (LoggerContext) provider.getLoggerFactory();

        assertTrue(loggerFactory.isStarted());

    }
}
