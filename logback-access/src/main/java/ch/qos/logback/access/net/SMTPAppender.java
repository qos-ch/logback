/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.access.net;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.SMTPAppenderBase;

/**
 * Send an e-mail when a specific access event occurs, typically on server errors.
 * 
 * <p>
 * The number of access events delivered in this e-mail depend on the value of
 * <b>BufferSize</b> option. The <code>SMTPAppender</code> keeps only the
 * last <code>BufferSize</code> access events in its cyclic buffer. This
 * keeps memory requirements at a reasonable level while still delivering useful
 * application context.
 * <p> 
 * By default, the email is sent everytime an event has a status code of 
 * <em>500 (server error) or higher</em>. In order not to flood one's mailbox, 
 * an email will be sent only if the previous email was sent more that 24 hours ago.
 * <p>
 * This behaviour can be easily bypassed either by modifying this class, or by
 * imlementing a new <code>EventEvaluator</code>.
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SMTPAppender extends SMTPAppenderBase {

  static final String DEFAULT_SUBJECT_PATTERN = "%m";
  
  private int bufferSize = 512;
  protected CyclicBuffer cb = new CyclicBuffer(bufferSize);

  /**
   * The default constructor will instantiate the appender with a
   * {@link EventEvaluator} that will trigger on events with level
   * ERROR or higher.
   */
  public SMTPAppender() {
    DefaultSMTPEvaluator evaluator = new DefaultSMTPEvaluator(getContext());
    setEventEvaluator(evaluator);
  }

  /**
   * Use <code>evaluator</code> passed as parameter as the {@link
   * EventEvaluator} for this SMTPAppender.
   */
  public SMTPAppender(EventEvaluator evaluator) {
    this.eventEvaluator = evaluator;
  }
  
  @Override
  public void start() {
    ((DefaultSMTPEvaluator) eventEvaluator).start();
    super.start();
  }

  /**
   * Perform SMTPAppender specific appending actions, mainly adding the event to
   * a cyclic buffer.
   */
  protected void subAppend(Object eventObject) {
    AccessEvent event = (AccessEvent) eventObject;

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
