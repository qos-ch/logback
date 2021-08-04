package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertNull;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

public class XCompositeConverter extends CompositeConverter<ILoggingEvent>{

	void assertNoNext() {
		assertNull("converter instance has next element", this.getNext());
	}

	@Override
	protected String transform(ILoggingEvent event, String in) {
		if (event.getMessage().contains("assert"))
			assertNoNext();
		return "";
	}

}
