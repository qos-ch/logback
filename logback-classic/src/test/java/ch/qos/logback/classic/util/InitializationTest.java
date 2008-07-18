package ch.qos.logback.classic.util;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;

public class InitializationTest extends TestCase {

  org.slf4j.Logger logger = LoggerFactory.getLogger(InitializationTest.class);
  LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
  Logger root = (Logger) LoggerFactory.getLogger("root");

  protected void setUp() throws Exception {
    super.setUp();
    logger.debug("Hello-didily-odily");

  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
  }

  public void testAutoconfig() {
    Appender appender = root.getAppender("STDOUT");
    assertNotNull(appender);
    assertTrue(appender instanceof ConsoleAppender);
  }

  public void testReset() {
    {
      Appender appender = root.getAppender("STDOUT");
      assertNotNull(appender);
      assertTrue(appender instanceof ConsoleAppender);
    }
    {
      lc.shutdownAndReset();
      Appender appender = root.getAppender("STDOUT");
      assertNull(appender);
    }
  }

  public void testAutoConfigFromSystemProperties() throws JoranException  {
    doAutoConfigFromSystemProperties(TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml");
    doAutoConfigFromSystemProperties("autoConfigAsResource.xml");
    // test passing a URL. note the relative path syntax with file:src/test/...
    doAutoConfigFromSystemProperties("file:"+TeztConstants.TEST_DIR_PREFIX + "input/autoConfig.xml"); 
  }
  public void doAutoConfigFromSystemProperties(String val) throws JoranException {
    lc.shutdownAndReset();
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, val);
    ContextInitializer.autoConfig(lc);
    Appender appender = root.getAppender("AUTO_BY_SYSTEM_PROPERTY");
    assertNotNull(appender);
  }
}
