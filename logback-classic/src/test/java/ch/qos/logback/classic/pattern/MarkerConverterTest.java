package ch.qos.logback.classic.pattern;

import junit.framework.TestCase;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

public class MarkerConverterTest extends TestCase {
  
  LoggerContext lc;
  Marker marker;
  MarkerConverter converter;
  
  public void setUp() throws Exception {
    lc = new LoggerContext();
    converter = new MarkerConverter();
    converter.start();
  }
  
  public void tearDown() throws Exception {
    lc = null;
    converter.stop();
    converter = null;
  }

  public void testWithNullMarker() {
    marker = null;
    String result = converter.convert(createLoggingEvent());
    assertEquals("", result);
  }
  
  public void testWithMarker() {
    String name = "test";
    marker = MarkerFactory.getMarker(name);
    String result = converter.convert(createLoggingEvent());
    assertEquals(name, result);
  }
  
  public void testWithOneChildMarker() {
    marker = MarkerFactory.getMarker("test");
    marker.add(MarkerFactory.getMarker("child"));
    
    String result = converter.convert(createLoggingEvent());
    
    assertEquals("test [ child ]", result);
  }
  
  public void testWithSeveralChildMarker() {
    marker = MarkerFactory.getMarker("testParent");
    marker.add(MarkerFactory.getMarker("child1"));
    marker.add(MarkerFactory.getMarker("child2"));
    marker.add(MarkerFactory.getMarker("child3"));
    
    String result = converter.convert(createLoggingEvent());
    
    assertEquals("testParent [ child1, child2, child3 ]", result);
  }
  
  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), lc.getLogger(LoggerContext.ROOT_NAME),
        Level.DEBUG, "test message", null, null);
    le.setMarker(marker);
    return le;
  }
}
