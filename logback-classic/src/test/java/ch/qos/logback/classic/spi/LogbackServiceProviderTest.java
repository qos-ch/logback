package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;

public class LogbackServiceProviderTest {

	
	LogbackServiceProvider provider = new LogbackServiceProvider();
	
	@Test
	public void testContrxtStart() {
		provider.initialize();
		LoggerContext loggerFactory = (LoggerContext) provider.getLoggerFactory();
		
		assertTrue(loggerFactory.isStarted());
		
	}
}
