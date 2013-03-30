package ch.qos.logback.classic.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContextInitializerAutoConfigTest {

  org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
  Logger root = (Logger) LoggerFactory.getLogger("root");

  @Before
  public void setUp() throws Exception {
    logger.debug("Hello-didily-odily");
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    System.clearProperty(ContextInitializer.STATUS_LISTENER_CLASS);
  }

  @Test
  @Ignore
  // this test works only if logback-test.xml or logback.xml files are on the classpath.
  // However, this is something we try to avoid in order to simplify the life
  // of users trying to follows the manual and logback-examples from an IDE
  public void autoconfig() {
    LoggerContext iLoggerFactory = (LoggerContext) LoggerFactory.getILoggerFactory();
    iLoggerFactory.reset();
    Appender<ILoggingEvent> appender = root.getAppender("STDOUT");
    assertNotNull(appender);
    assertTrue(appender instanceof ConsoleAppender);
  }
}
