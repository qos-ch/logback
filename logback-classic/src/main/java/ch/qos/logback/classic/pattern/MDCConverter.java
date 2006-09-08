package ch.qos.logback.classic.pattern;

import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;

public class MDCConverter extends ClassicConverter {
	
	public MDCConverter() {
	}

	@Override
	public String convert(Object event) {
		LoggingEvent loggingEvent = (LoggingEvent)event;
		Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
		if(mdcPropertyMap != null) {
			return loggingEvent.getMDCPropertyMap().get(getFirstOption());
		} else {
			return "";
		}
	}
}


