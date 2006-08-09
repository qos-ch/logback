package ch.qos.logback;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;

public class BasicConfigurator {

	public static void configure(LoggerContext lc) {
		ConsoleAppender ca = new ConsoleAppender();
		ca.setContext(lc);
		ca.setName("console");
		PatternLayout pl = new PatternLayout();
		pl.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
		pl.start();

		ca.setLayout(pl);
		ca.start();
		Logger rootLogger = lc.getLogger(LoggerContext.ROOT_NAME);
		rootLogger.addAppender(ca);
	}
	
	public static void configureDefaultContext() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		configure(lc);
	}
}
