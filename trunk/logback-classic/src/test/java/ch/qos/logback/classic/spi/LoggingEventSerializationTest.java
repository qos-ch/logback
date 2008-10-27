package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class LoggingEventSerializationTest {

  LoggerContext lc;
  Logger logger;

  ByteArrayOutputStream bos;
  ObjectOutputStream oos;
  ObjectInputStream inputStream;

  @Before
  public void setUp() throws Exception {
    lc = new LoggerContext();
    lc.setName("testContext");
    logger = lc.getLogger(LoggerContext.ROOT_NAME);
    // create the byte output stream
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);
  }

  @After
  public void tearDown() throws Exception {
    lc = null;
    logger = null;
    oos.close();
  }

  @Test
  public void smoke() throws Exception {
    LoggingEvent event = createLoggingEvent();
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
  }

  @Test
  public void context() throws Exception {
    lc.putProperty("testKey", "testValue");
    LoggingEvent event = createLoggingEvent();
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);

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

  @Test
  public void MDC() throws Exception {
    MDC.put("key", "testValue");
    LoggingEvent event = createLoggingEvent();
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
    Map<String, String> MDCPropertyMap = remoteEvent.getMDCPropertyMap();
    assertEquals("testValue", MDCPropertyMap.get("key"));
  }

  @Test
  public void updatedMDC() throws Exception {
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

  @Test
  public void nonSerializableParameters() throws Exception {
    LoggingEvent event = createLoggingEvent();
    LuckyCharms lucky0 = new LuckyCharms(0);
    event.setArgumentArray(new Object[] { lucky0, null });
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
    
    Object[] aa = remoteEvent.getArgumentArray();
    assertNotNull(aa);
    assertEquals(2, aa.length);
    assertEquals("LC(0)", aa[0]);
    assertNull(aa[1]);
  }

  @Test
  public void _Throwable() throws Exception {
    LoggingEvent event = createLoggingEvent();
    Throwable throwable = new Throwable("just testing");
    ThrowableProxy tp = new ThrowableProxy(throwable);
    event.setThrowableProxy(tp);
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
  }

  @Test
  public void extendendeThrowable() throws Exception {
    LoggingEvent event = createLoggingEvent();
    Throwable throwable = new Throwable("just testing");
    ThrowableProxy tp = new ThrowableProxy(throwable);
    event.setThrowableProxy(tp);
    tp.calculatePackagingData();

    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
  }
  
  
  @Test
  public void serializeLargeArgs() throws Exception {
    
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 100000; i++) {
      buffer.append("X");
    }
    String largeString = buffer.toString();
    Object[] argArray = new Object[] {new LuckyCharms(2),
        largeString };
    
    LoggingEvent event = createLoggingEvent();
    event.setArgumentArray(argArray);
    
    LoggingEvent remoteEvent = writeAndRead(event);
    checkForEquality(event, remoteEvent);
    Object[] aa = remoteEvent.getArgumentArray();
    assertNotNull(aa);
    assertEquals(2, aa.length);
    String stringBack = (String) aa[1];
    assertEquals(largeString, stringBack);
  }

  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", null, null);
    return le;
  }

  private void checkForEquality(LoggingEvent original,
      LoggingEvent afterSerialization) {
    assertEquals(original.getLevel(), afterSerialization.getLevel());
    assertEquals(original.getFormattedMessage(), afterSerialization
        .getFormattedMessage());
    assertEquals(original.getMessage(), afterSerialization.getMessage());
    
    System.out.println();
    
    assertEquals(original.getThrowableProxy(), afterSerialization
        .getThrowableProxy());

  }

  private LoggingEvent writeAndRead(LoggingEvent event) throws IOException,
      ClassNotFoundException {
    oos.writeObject(event);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    inputStream = new ObjectInputStream(bis);

    return (LoggingEvent) inputStream.readObject();
  }

}
