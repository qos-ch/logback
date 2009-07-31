package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.util.SystemInfo;

public class MDCConverterTest {

  LoggerContext lc;
  MDCConverter converter;

  @Before
  public void setUp() throws Exception {
    lc = new LoggerContext();
    converter = new MDCConverter();
    converter.start();
  }

  @After
  public void tearDown() throws Exception {
    lc = null;
    converter.stop();
    converter = null;
  }

  @Test
  public void testConverWithOneEntry() {
    MDC.clear();
    MDC.put("testKey", "testValue");
    ILoggingEvent le = createLoggingEvent();
    String result = converter.convert(le);
    assertEquals("testKey=testValue", result);
  }

  @Test
  public void testConverWithMultipleEntries() {
    MDC.clear();
    MDC.put("testKey", "testValue");
    MDC.put("testKey2", "testValue2");
    ILoggingEvent le = createLoggingEvent();
    String result = converter.convert(le);
    if (SystemInfo.getJavaVendor().contains("IBM")) {
      assertEquals("testKey2=testValue2, testKey=testValue", result);
    } else {
      assertEquals("testKey=testValue, testKey2=testValue2", result);
    }
  }

  private ILoggingEvent createLoggingEvent() {
    ILoggingEvent le = new LoggingEvent(this.getClass().getName(), lc
        .getLogger(Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", null,
        null);
    return le;
  }
}
