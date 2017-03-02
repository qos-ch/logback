package ch.qos.logback.classic.json;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ConverterTest;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;

/**
 * @author Pierre Queinnec
 */
public class JsonLayoutTest {

  LogbackMDCAdapter lma;
  LoggerContext lc;
  Logger logger;
  Logger root;
  JsonLayout layout;

  @Before
  public void setUp() throws Exception {
    lma = new LogbackMDCAdapter();
    lc = new LoggerContext();
    logger = lc.getLogger(ConverterTest.class);
    lc.setName("default");

    layout = new JsonLayout();
    // layout.setThrowableRenderer(new DefaultThrowableRenderer());
    layout.setContext(lc);
    layout.start();

    root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
  }

  @After
  public void tearDown() throws Exception {
    MDC.clear();
    lc = null;
    layout = null;
  }

  @Test
  public void testDefaultsWithoutMDC() {
    String correctResultRegex = "\\{\"level\":\"INFO\","
        + "\"logger\":\"ch.qos.logback.classic.pattern.ConverterTest\""
        + ",\"formatted-message\":\"Some message\"" + ",\"timestamp\":\\d+\\}";

    String result = layout.doLayout(makeLoggingEvent(null));
    System.out.println(result);

    assertTrue(result.matches(correctResultRegex));
  }

  @Test
  public void testEventHasMDCWithConfigWithoutMDC() {
    layout.setIncludeMDC(false);

    String correctResultRegex = "\\{\"level\":\"INFO\","
        + "\"logger\":\"ch.qos.logback.classic.pattern.ConverterTest\""
        + ",\"formatted-message\":\"Some message\"" + ",\"timestamp\":\\d+\\}";

    String result = layout.doLayout(makeLoggingEventWithMDC(null));
    System.out.println(result);

    assertTrue(result.matches(correctResultRegex));
  }

  @Test
  public void testEventHasMDCWithConfigWithMDC() {
    layout.setIncludeMDC(true);

    String correctResultRegex = "\\{\"level\":\"INFO\","
        + "\"logger\":\"ch.qos.logback.classic.pattern.ConverterTest\""
        + ",\"formatted-message\":\"Some message\""
        + ",\"mdc\":\\{\"suspicious\":\"true\",\"user\":\"joe\"\\}"
        + ",\"timestamp\":\\d+\\}";

    String result = layout.doLayout(makeLoggingEventWithMDC(null));
    System.out.println(result);

    assertTrue(result.matches(correctResultRegex));
  }

  // TODO test escaping the message in context

  private ILoggingEvent makeLoggingEvent(Exception ex) {
    return new LoggingEvent(
        ch.qos.logback.core.pattern.FormattingConverter.class.getName(),
        logger, Level.INFO, "Some message", ex, null);
  }

  private ILoggingEvent makeLoggingEventWithMDC(Exception ex) {
    MDC.put("user", "joe");
    MDC.put("suspicious", Boolean.TRUE.toString());

    ILoggingEvent event = this.makeLoggingEvent(ex);
    return event;
  }

}
