package ch.qos.logback.classic.util;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.net.SyslogConstants;

public class LevelToSyslogSeverityTest {

  @Test
  public void smoke() {

    assertEquals(SyslogConstants.DEBUG_SEVERITY, LevelToSyslogSeverity
        .convert(createEventOfLevel(Level.TRACE)));

    assertEquals(SyslogConstants.DEBUG_SEVERITY, LevelToSyslogSeverity
        .convert(createEventOfLevel(Level.DEBUG)));

    assertEquals(SyslogConstants.INFO_SEVERITY, LevelToSyslogSeverity
        .convert(createEventOfLevel(Level.INFO)));

    assertEquals(SyslogConstants.WARNING_SEVERITY, LevelToSyslogSeverity
        .convert(createEventOfLevel(Level.WARN)));

    assertEquals(SyslogConstants.ERROR_SEVERITY, LevelToSyslogSeverity
        .convert(createEventOfLevel(Level.ERROR)));

  }

  LoggingEvent createEventOfLevel(Level level) {
    LoggingEvent event = new LoggingEvent();
    event.setLevel(level);
    return event;
  }

}
