package ch.qos.logback.classic.gaffer

import ch.qos.logback.classic.LoggerContext
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*
import ch.qos.logback.core.status.StatusChecker
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter
import ch.qos.logback.classic.Level
import ch.qos.logback.core.testUtil.RandomUtil
import ch.qos.logback.classic.Logger
import ch.qos.logback.core.Appender
import ch.qos.logback.core.helpers.NOPAppender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.core.util.StatusPrinter
import ch.qos.logback.classic.net.SMTPAppender

/**
 * @author Ceki G&uuml;c&uuml;
 */
class ConfigurationDelegateTest {

  LoggerContext context = new LoggerContext()
  ConfigurationDelegate configurationDelegate = new ConfigurationDelegate();
  StatusChecker statusChecker = new StatusChecker(context)
  int diff = RandomUtil.getPositiveInt();

  @Before
  void setUp() {
    context.name = "ConfigurationDelegateTest"
    configurationDelegate.context = context;
  }

  @Test
  void contextAwareMixin() {
    configurationDelegate.addInfo("smoke")
    assertTrue(statusChecker.containsMatch("smoke"))
  }

  @Test
  void scan() {
    configurationDelegate.scan("10seconds")
    assertTrue(statusChecker.containsMatch("Setting ReconfigureOnChangeFilter"))
    assertTrue(statusChecker.containsMatch("Adding ReconfigureOnChangeFilter as a turbo filter"))

    TurboFilter filter = context.turboFilterList[0]
    assertTrue(filter instanceof ReconfigureOnChangeFilter)
    ReconfigureOnChangeFilter rocf = (ReconfigureOnChangeFilter) filter;
    assertEquals(10 * 1000, rocf.refreshPeriod)
  }

  @Test
  void timestamp() {
    String result = configurationDelegate.timestamp("yyyy")
    long year = Calendar.getInstance().get(Calendar.YEAR);
    assertEquals(year.toString(), result)
  }


  @Test
  void loggerWithoutName() {
    configurationDelegate.logger("", Level.DEBUG)
    assertTrue(statusChecker.containsMatch("No name attribute for logger"))
  }

  @Test
  void loggerSetLevel() {
    configurationDelegate.logger("setLevel" + diff, Level.INFO)
    Logger smokeLogger = context.getLogger("setLevel" + diff);
    assertEquals(Level.INFO, smokeLogger.level)
  }


  @Test
  void loggerAppenderRef() {
    Appender fooAppender = new NOPAppender();
    fooAppender.name = "FOO"
    configurationDelegate.appenderList = [fooAppender]
    configurationDelegate.logger("test" + diff, Level.INFO, ["FOO"])
    Logger logger = context.getLogger("test" + diff);
    assertEquals(Level.INFO, logger.level)
    assertEquals(fooAppender, logger.getAppender("FOO"))
  }

  @Test
  void loggerAdditivity() {
    Appender fooAppender = new NOPAppender();
    fooAppender.name = "FOO"
    configurationDelegate.appenderList = [fooAppender]
    configurationDelegate.logger("test" + diff, Level.INFO, ["FOO"], false)
    Logger logger = context.getLogger("test" + diff);
    assertEquals(Level.INFO, logger.level)
    assertEquals(fooAppender, logger.getAppender("FOO"))
    assertEquals(false, logger.additive)
  }

  @Test
  void loggerAdditivittWithEmptyList() {
    configurationDelegate.logger("test" + diff, Level.INFO, [], false)
    Logger logger = context.getLogger("test" + diff);
    assertEquals(Level.INFO, logger.level)
    assertEquals(null, logger.getAppender("FOO"))
    assertEquals(false, logger.additive)
  }

  @Test
  void root_LEVEL() {
    configurationDelegate.root(Level.ERROR)
    Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
    assertEquals(Level.ERROR, root.level)
    assertEquals(null, root.getAppender("FOO"))
  }

  @Test
  void root_WithList() {
    Appender fooAppender = new NOPAppender();
    fooAppender.name = "FOO"
    configurationDelegate.appenderList = [fooAppender]
    configurationDelegate.root(Level.WARN, ["FOO"])
    Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
    assertEquals(Level.WARN, root.level)
    assertEquals(fooAppender, root.getAppender("FOO"))
  }

  @Test
  void appender0() {
    configurationDelegate.appender("A", NOPAppender);
    Appender back = configurationDelegate.appenderList.find {it.name = "A"}
    assertNotNull(back)
    assertEquals("A", back.name)
  }

  @Test
  void appender1() {
    configurationDelegate.appender("C", ConsoleAppender) {
      target = "System.err"
    }
    Appender back = configurationDelegate.appenderList.find {it.name = "C"}
    assertNotNull(back)
    assertEquals("C", back.name)
    assertEquals("System.err", back.target)
  }


  @Test
  void appenderWithEncoder() {
    configurationDelegate.appender("C", ConsoleAppender) {
      encoder (LayoutWrappingEncoder) {
        layout (PatternLayout) {
          pattern = "%m%n"
        }
      }
    }
    Appender back = configurationDelegate.appenderList.find {it.name = "C"}
    assertNotNull(back)
    assertEquals("C", back.name)
    ConsoleAppender ca = back
    assertNotNull(ca.encoder)
    assertNotNull(ca.encoder.layout)
    PatternLayout layout =  ca.encoder.layout
    assertEquals("%m%n", layout.pattern)

  }

    @Test
  void appenderSMTP() {
    configurationDelegate.appender("SMTP", SMTPAppender) {
      to = "a"
      to = "b"
      layout (PatternLayout) {
          pattern = "%m%n"
      }
    }
       StatusPrinter.print context
    Appender back = configurationDelegate.appenderList.find {it.name = "SMTP"}
    assertNotNull(back)
    assertEquals("SMTP", back.name)
    SMTPAppender sa = back
    PatternLayout layout =  sa.layout
    assertEquals("%m%n", layout.pattern)

    assertEquals(["a", "b"], sa.toList.sort());

  }
}
