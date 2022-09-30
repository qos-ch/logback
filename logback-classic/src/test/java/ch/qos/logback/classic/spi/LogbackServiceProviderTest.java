package ch.qos.logback.classic.spi;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogbackServiceProviderTest {

    LogbackServiceProvider provider = new LogbackServiceProvider();

    @Test
    public void testContrxtStart() {
        provider.initialize();
        LoggerContext loggerFactory = (LoggerContext) provider.getLoggerFactory();

        assertTrue(loggerFactory.isStarted());

    }
}
