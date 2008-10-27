package ch.qos.logback.classic.net.testObjectBuilders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEventExt;

public class LoggingEventExtBuilder implements Builder {

	public Object build(int i) {
		LoggingEventExt le = new LoggingEventExt();
		le.setLevel(Level.DEBUG);
		le.setLogger(new LoggerContext().getLogger(LoggerContext.ROOT_NAME));
		le.setMessage(MSG_PREFIX + i);
		le.setThreadName("threadName");
		return le;
	}
}
