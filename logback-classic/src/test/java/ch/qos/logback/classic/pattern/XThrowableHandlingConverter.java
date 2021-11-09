package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertNull;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class XThrowableHandlingConverter extends ThrowableHandlingConverter {

	void assertNoNext() {
		assertNull("has next", getNext());
	}

	@Override
	public String convert(final ILoggingEvent event) {
		if (event.getMessage().contains("assert")) {
			assertNoNext();
		}
		return "";
	}

}
