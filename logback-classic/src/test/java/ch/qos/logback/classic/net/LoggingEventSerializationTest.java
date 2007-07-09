package ch.qos.logback.classic.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LoggingEventSerializationTest extends TestCase {

  LoggerContext lc;
  Logger logger;

  ByteArrayOutputStream bos;
  ObjectOutputStream oos;
  ObjectInputStream inputStream;

  public void setUp() throws Exception {
    super.setUp();
    lc = new LoggerContext();
    lc.setName("testContext");
    logger = lc.getLogger(LoggerContext.ROOT_NAME);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    lc = null;
    logger = null;
  }

  public void testBasic() throws Exception {
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);

    LoggingEvent event = createLoggingEvent();
    oos.writeObject(event);

    // create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();

    assertEquals("test message", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  public void testContext() throws Exception {
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);

    lc.setProperty("testKey", "testValue");
    LoggingEvent event = createLoggingEvent();
    oos.writeObject(event);

    // create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();

    LoggerRemoteView loggerRemoteView = remoteEvent.getLoggerRemoteView();
    assertNotNull(loggerRemoteView);
    assertEquals("root", loggerRemoteView.getName());

    LoggerContextRemoteView loggerContextRemoteView = loggerRemoteView
        .getLoggerContextView();
    assertNotNull(loggerContextRemoteView);
    assertEquals("testContext", loggerContextRemoteView.getName());
    Map<String, String> props = loggerContextRemoteView.getPropertyMap();
    assertNotNull(props);
    assertEquals("testValue", props.get("testKey"));
  }
  
  public void testMDC() throws Exception {
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);

    MDC.put("key", "testValue");
    LoggingEvent event = createLoggingEvent();
    oos.writeObject(event);

    // create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();   

    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
  }

  public void testUpdatedMDC() throws Exception {
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);

    MDC.put("key", "testValue");
    LoggingEvent event1 = createLoggingEvent();
    oos.writeObject(event1);
    
    MDC.put("key", "updatedTestValue");
    LoggingEvent event2 = createLoggingEvent();
    oos.writeObject(event2);

    // create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    // skip over one object
    inputStream.readObject();      
    LoggingEvent remoteEvent2 = (LoggingEvent) inputStream.readObject();  
    
    // We observe the second logging event. It should provide us with
    // the updated MDC property.
    Map<String, String> MDCPropertyMap = remoteEvent2.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
  }
  
  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", null, null);
    return le;
  }

}
