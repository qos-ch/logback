package ch.qos.logback.classic.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;

public class SocketAppenderTest extends TestCase {

  ByteArrayOutputStream bos;
  ObjectInputStream inputStream;
  SocketAppender appender;
  Logger logger;

  LoggerContext lc;
  MockSocketServer mockSocketServer;

  public void setUp() throws Exception {
    lc = new LoggerContext();
    lc.setName("test");
    lc.setProperty("testKey", "testValue");
    appender = new SocketAppender();
    appender.setPort(123);
    appender.setContext(lc);
    appender.setRemoteHost("localhost");
    appender.start();
    logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.addAppender(appender);
  }

  public void testStartFailNoRemoteHost() {
    lc = new LoggerContext();
    SocketAppender appender = new SocketAppender();
    appender.setContext(lc);
    appender.setPort(123);
    appender.start();
    assertEquals(1, lc.getStatusManager().getCount());
  }

  public void testRecieveMessage() throws InterruptedException, IOException,
      ClassNotFoundException {

    //create the byte output stream
    bos = new ByteArrayOutputStream() ;
    appender.oos = new ObjectOutputStream(bos);
    
    LoggingEvent event = createLoggingEvent();
    appender.append(event);

    //create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);
    
    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();

    assertEquals("test message", remoteEvent.getMessage());
    assertEquals(Level.DEBUG, remoteEvent.getLevel());
  }

  public void testRecieveWithContext() throws InterruptedException, IOException, ClassNotFoundException {

    //create the byte output stream
    bos = new ByteArrayOutputStream() ;
    appender.oos = new ObjectOutputStream(bos);
    
    LoggingEvent event = createLoggingEvent();
    appender.append(event);

    //create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);
    
    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();

    LoggerRemoteView loggerRemoteView = remoteEvent.getLoggerRemoteView();
    assertNotNull(loggerRemoteView);
    assertEquals("root", loggerRemoteView.getName());

    LoggerContextRemoteView loggerContextRemoteView = loggerRemoteView
        .getLoggerContextView();
    assertNotNull(loggerContextRemoteView);
    assertEquals("test", loggerContextRemoteView.getName());
    Map<String, String> props = loggerContextRemoteView.getPropertyMap();
    assertEquals("testValue", props.get("testKey"));
  }

  public void testMessageWithMDC() throws InterruptedException, IOException, ClassNotFoundException {
    //create the byte output stream
    bos = new ByteArrayOutputStream() ;
    appender.oos = new ObjectOutputStream(bos);

    MDC.put("key", "testValue");
    LoggingEvent event = createLoggingEvent();
    appender.append(event);

    //create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);
    
    LoggingEvent remoteEvent = (LoggingEvent) inputStream.readObject();
    
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
  }

  public void testMessageWithUpdatedMDC() throws InterruptedException, IOException, ClassNotFoundException {
    //create the byte output stream
    bos = new ByteArrayOutputStream() ;
    appender.oos = new ObjectOutputStream(bos);

    MDC.put("key", "testValue");
    LoggingEvent event = createLoggingEvent();
    appender.append(event);

    MDC.put("key", "updatedTestValue");
    event = createLoggingEvent();
    appender.append(event);

    //create the input stream based on the ouput stream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);
    
    @SuppressWarnings("unused")
    LoggingEvent remoteEvent1 = (LoggingEvent) inputStream.readObject();
    LoggingEvent remoteEvent2 = (LoggingEvent) inputStream.readObject();

    Map<String, String> MDCPropertyMap = remoteEvent2.getMDCPropertyMap();
    assertEquals("updatedTestValue", MDCPropertyMap.get("key"));
  }

  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", null, null);
    return le;
  }
}
