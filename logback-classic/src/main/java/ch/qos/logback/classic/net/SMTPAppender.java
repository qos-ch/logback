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

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.SMTPAppenderBase;

/**
 * Send an e-mail when a specific logging event occurs, typically on errors or
 * fatal errors.
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SMTPAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SMTPAppender extends SMTPAppenderBase<LoggingEvent> {

  static final String DEFAULT_SUBJECT_PATTERN = "%logger{20} - %m %nopex";
  static final String DEFAULT_EVALUATOR_EXPRESSION = "level >= ERROR";
  
  private int bufferSize = 512;
  protected CyclicBuffer<LoggingEvent> cb = new CyclicBuffer<LoggingEvent>(bufferSize);

  /**
   * The default constructor will instantiate the appender with a
   * {@link EventEvaluator} that will trigger on events with level
   * ERROR or higher.
   */
  public SMTPAppender() {

  }
  
  public void start() {    
    if (eventEvaluator == null) {
      JaninoEventEvaluator jee = new JaninoEventEvaluator();
      jee.setContext(getContext());
      jee.setExpression(DEFAULT_EVALUATOR_EXPRESSION);
      jee.setName("SMTPAppender's default event evaluator");
      jee.start();
      this.eventEvaluator = jee;      
    }
    super.start();
  }

  /**
   * Use the parameter as the {@link
   * EventEvaluator} for this SMTPAppender.
   */
  public SMTPAppender(EventEvaluator eventEvaluator) {
    this.eventEvaluator = eventEvaluator;
  }

  /**
   * Perform SMTPAppender specific appending actions, mainly adding the event to
   * a cyclic buffer.
   */
  protected void subAppend(LoggingEvent event) {
    event.getThreadName();
    cb.add(event);
    // addInfo("Added event to the cyclic buffer: " + event.getMessage());
  }

  @Override
  protected void fillBuffer(StringBuffer sbuf) {
    int len = cb.length();
    for (int i = 0; i < len; i++) {
      // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
      LoggingEvent event = cb.get();
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
  protected Layout<LoggingEvent> makeSubjectLayout(String subjectStr) {
    if(subjectStr == null) {
      subjectStr = DEFAULT_SUBJECT_PATTERN;
    }
    PatternLayout pl = new PatternLayout();
    pl.setContext(getContext());
    pl.setPattern(subjectStr);
    // we don't want a ThrowableInformationConverter appended
    // to the end of the converter chain
    // This fixes issue LBCLASSIC-67
    pl.setPostCompileProcessor(null);
    pl.start();
    return pl;
  }
}
