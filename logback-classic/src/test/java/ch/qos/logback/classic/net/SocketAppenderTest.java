package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class SocketAppenderTest {


	public static void main(String[] args) {
		
		Logger logger = (Logger) LoggerFactory.getLogger(SocketAppenderTest.class);
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		SocketAppender appender = new SocketAppender("localhost", 4560);
		appender.setContext(lc);
		appender.setName("socket");
		appender.start();

		logger.addAppender(appender);

		logger.debug("************* Hello world.");

	}

}
