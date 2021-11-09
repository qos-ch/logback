package ch.qos.logback.classic.layout;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class TTLLLayoutTest {

	LoggerContext context = new LoggerContext();
	Logger logger = context.getLogger(TTLLLayoutTest.class);
	TTLLLayout layout = new TTLLLayout();

	@Before
	public void setUp() {
		layout.setContext(context);
		layout.start();
	}

	@Test
	public void nullMessage() {
		final LoggingEvent event = new LoggingEvent("", logger, Level.INFO, null, null, null);
		event.setTimeStamp(0);
		final String result = layout.doLayout(event);

		final String resultSuffix = result.substring(13).trim();

		assertTrue("[" + resultSuffix + "] did not match regexs", resultSuffix.matches("\\[.*\\] INFO ch.qos.logback.classic.layout.TTLLLayoutTest - null"));
	}
}
