package ch.qos.logback.classic.servlet;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.StatusViaSLF4JLoggerFactory;
import ch.qos.logback.core.spi.ContextAwareBase;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Allows for graceful shutdown of the {@link LoggerContext} associated with this web-app.
 *
 * @author Ceki Gulcu
 * @since 1.1.10
 */
public class LogbackServletContextListener implements ServletContextListener {

	ContextAwareBase contextAwareBase = new ContextAwareBase();

	@Override
	public void contextInitialized(final ServletContextEvent sce) {

	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {

		final ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
		if (iLoggerFactory instanceof LoggerContext) {
			final LoggerContext loggerContext = (LoggerContext) iLoggerFactory;
			contextAwareBase.setContext(loggerContext);
			StatusViaSLF4JLoggerFactory.addInfo("About to stop " + loggerContext.getClass().getCanonicalName() + " [" + loggerContext.getName() + "]", this);
			loggerContext.stop();
		}
	}

}
