/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.net;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnErrorEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
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
public class SMTPAppender extends SMTPAppenderBase<ILoggingEvent> {

  // value "%logger{20} - %m" is referenced in the docs!
  static final String DEFAULT_SUBJECT_PATTERN = "%logger{20} - %m";
  
  private int bufferSize = 512;
  protected CyclicBuffer<ILoggingEvent> cb = new CyclicBuffer<ILoggingEvent>(bufferSize);

  /**
   * The default constructor will instantiate the appender with a
   * {@link EventEvaluator} that will trigger on events with level
   * ERROR or higher.
   */
  public SMTPAppender() {

  }
  
  public void start() {    
    if (eventEvaluator == null) {
      OnErrorEvaluator onError = new OnErrorEvaluator();
      onError.setContext(getContext());
      onError.setName("onError");
      onError.start();
      this.eventEvaluator = onError;      
    }
    super.start();
  }

  /**
   * Use the parameter as the {@link
   * EventEvaluator} for this SMTPAppender.
   */
  public SMTPAppender(EventEvaluator<ILoggingEvent> eventEvaluator) {
    this.eventEvaluator = eventEvaluator;
  }

  /**
   * Perform SMTPAppender specific appending actions, mainly adding the event to
   * a cyclic buffer.
   */
  protected void subAppend(ILoggingEvent event) {
    event.prepareForDeferredProcessing();
    cb.add(event);
    // addInfo("Added event to the cyclic buffer: " + event.getMessage());
  }

  @Override
  protected void fillBuffer(StringBuffer sbuf) {
    int len = cb.length();
    for (int i = 0; i < len; i++) {
      // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
      ILoggingEvent event = cb.get();
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
  protected Layout<ILoggingEvent> makeSubjectLayout(String subjectStr) {
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
