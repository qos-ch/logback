/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.net;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.net.SMTPAppenderBase;
import ch.qos.logback.core.rolling.TriggeringPolicy;

/**
 * Send an e-mail when a specific logging event occurs, typically on errors or
 * fatal errors.
 * 
 * <p>
 * The number of logging events delivered in this e-mail depend on the value of
 * <b>BufferSize</b> option. The <code>SMTPAppender</code> keeps only the
 * last <code>BufferSize</code> logging events in its cyclic buffer. This
 * keeps memory requirements at a reasonable level while still delivering useful
 * application context.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SMTPAppender extends SMTPAppenderBase {

  static final String DEFAULT_SUBJECT_PATTERN = "%logger{20} - %m";
  
  private int bufferSize = 512;
  protected CyclicBuffer cb = new CyclicBuffer(bufferSize);

  /**
   * The default constructor will instantiate the appender with a
   * {@link TriggeringPolicy} that will trigger on events with level
   * ERROR or higher.
   */
  public SMTPAppender() {
    this(new DefaultSMTPTriggeringPolicy());
  }

  /**
   * Use the parameter as the {@link
   * TriggeringPolicy} for this SMTPAppender.
   */
  public SMTPAppender(TriggeringPolicy triggeringPolicy) {
    this.triggeringPolicy = triggeringPolicy;
  }

  /**
   * Perform SMTPAppender specific appending actions, mainly adding the event to
   * a cyclic buffer.
   */
  protected void subAppend(Object eventObject) {
    LoggingEvent event = (LoggingEvent) eventObject;

    event.getThreadName();
    cb.add(event);
    // addInfo("Added event to the cyclic buffer: " + event.getMessage());
  }

  @Override
  protected void fillBuffer(StringBuffer sbuf) {
    int len = cb.length();
    for (int i = 0; i < len; i++) {
      // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
      Object event = cb.get();
      sbuf.append(layout.doLayout(event));
    }
  }

  /**
   * The <b>BufferSize</b> option takes a positive integer representing the
   * maximum number of logging events to collect in a cyclic buffer. When the
   * <code>BufferSize</code> is reached, oldest events are deleted as new
   * events are added to the buffer. By default the size of the cyclic buffer is
   * 512 events.
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    cb.resize(bufferSize);
  }

  /**
   * Returns value of the <b>BufferSize</b> option.
   */
  public int getBufferSize() {
    return bufferSize;
  }

  @Override
  protected Layout makeSubjectLayout(String subjectStr) {
    if(subjectStr == null) {
      subjectStr = DEFAULT_SUBJECT_PATTERN;
    }
    PatternLayout pl = new PatternLayout();
    pl.setPattern(subjectStr);
    pl.start();
    return pl;
  }
}

class DefaultSMTPTriggeringPolicy implements TriggeringPolicy {

  private boolean started;

  /**
   * Is this <code>event</code> the e-mail triggering event?
   * 
   * <p>
   * This method returns <code>true</code>, if the event level has ERROR
   * level or higher. Otherwise it returns <code>false</code>.
   */
  public boolean isTriggeringEvent(File activeFile, Object eventObject) {
    LoggingEvent event = (LoggingEvent) eventObject;
    return event.getLevel().isGreaterOrEqual(Level.ERROR);
  }

  public boolean isStarted() {
    return started == true;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }
}
