package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent2;

public class LoggingEvent2Builder implements Builder {

	public Object build(int i) {
		LoggingEvent2 le = new LoggingEvent2();
		le.setLevel(Level.DEBUG);
		le.setLogger(new LoggerContext().getLogger(LoggerContext.ROOT_NAME));
		// 45 characters message
		le.setMessage(MSG_PREFIX + i);
		le.setThreadName("threadName");
		return le;
	}
}
