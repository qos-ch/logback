package ch.qos.logback.classic.encoder;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

public class PatternLayoutEncoderTest {

  PatternLayoutEncoder ple = new PatternLayoutEncoder();
  LoggerContext context = new LoggerContext();
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  Logger logger = context.getLogger(PatternLayoutEncoderTest.class);
  Charset utf8Charset = Charset.forName("UTF-8");
  
  @Before
  public void setUp() {
    ple.setPattern("%m");
    ple.setContext(context);
  }

  ILoggingEvent makeLoggingEvent(String message) {
    return new LoggingEvent("", logger, Level.DEBUG, message, null, null);
  }

  @Test
  public void smoke() throws IOException {
    ple.start();
    ple.init(baos);
    String msg = "hello";
    ILoggingEvent event = makeLoggingEvent(msg);
    ple.doEncode(event);
    ple.close();
    assertEquals(msg, baos.toString());
  }

  @Test
  public void charset() throws IOException {
    ple.setCharset(utf8Charset);
    ple.start();
    ple.init(baos);
    String msg = "\u03b1";
    ILoggingEvent event = makeLoggingEvent(msg);
    ple.doEncode(event);
    ple.close();
    assertEquals(msg, new String(baos.toByteArray(), utf8Charset.name()));
  }

}
