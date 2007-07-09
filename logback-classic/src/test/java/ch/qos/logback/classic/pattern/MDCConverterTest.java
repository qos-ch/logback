package ch.qos.logback.classic.pattern;

import junit.framework.TestCase;

import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class MDCConverterTest extends TestCase {
  
  LoggerContext lc;
  MDCConverter converter;
  
  public void setUp() throws Exception {
    lc = new LoggerContext();
    converter = new MDCConverter();
    converter.start();
  }
  
  public void tearDown() throws Exception {
    lc = null;
    converter.stop();
    converter = null;
  }
  
  public void testConverWithOneEntry() {
    MDC.clear();
    MDC.put("testKey", "testValue");
    LoggingEvent le = createLoggingEvent();
    String result = converter.convert(le);
    assertEquals("testKey=testValue", result);
  }

  public void testConverWithMultipleEntries() {
    MDC.clear();
    MDC.put("testKey", "testValue");
    MDC.put("testKey2", "testValue2");
    LoggingEvent le = createLoggingEvent();
    String result = converter.convert(le);
    assertEquals("testKey=testValue, testKey2=testValue2", result);
  }
  
  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), lc.getLogger(LoggerContext.ROOT_NAME),
        Level.DEBUG, "test message", null, null);
    return le;
  }
}
