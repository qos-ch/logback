package ch.qos.logback.classic.util;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;


public class LoggerStatusPrinter {
	
	public static void printStatusInDefaultContext() {
		Logger logger = (Logger)LoggerFactory.getLogger(LoggerStatusPrinter.class);
		LoggerContext lc = logger.getLoggerContext();
		StatusManager sm = lc.getStatusManager();
    StatusPrinter.print(sm);
	}
	
}
