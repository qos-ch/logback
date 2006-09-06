package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingEventBuilder implements Builder {

	private Logger logger = new LoggerContext().getLogger(LoggerContext.ROOT_NAME);
	
	public Object build(int i) {
		LoggingEvent le = new LoggingEvent();
		le.setLevel(Level.DEBUG);
		le.setLogger(logger);
		//le.setLogger(new LoggerContext().getLogger(LoggerContext.ROOT_NAME));
		le.setMessage(MSG_PREFIX + i);
		le.setThreadName("threadName");
		return le;
	}
}
