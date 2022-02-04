/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) <year>, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic;

import java.util.Iterator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;


/**
 * Appender which delegates all events to child appenders.
 *
 * @author Juan Pablo Santos Rodríguez
 * @see http://jira.qos.ch/browse/LOGBACK-551
 */
public class DelegateAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

  private AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<ILoggingEvent>();

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.AppenderBase#append(java.lang.Object)
   */
  @Override
  protected void append(ILoggingEvent eventObject) {
    Iterator<Appender<ILoggingEvent>> appenders = iteratorForAppenders();
    while(appenders.hasNext()) {
      appenders.next().doAppend(eventObject);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#addAppender(ch.qos.logback.core.Appender)
   */
  @Override
  public void addAppender(Appender< ILoggingEvent > newAppender) {
    addInfo("Attaching appender named [" + newAppender.getName() + "] to DelegateAppender.");
    aai.addAppender(newAppender);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#iteratorForAppenders()
   */
  @Override
  public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#getAppender(java.lang.String)
   */
  @Override
  public Appender< ILoggingEvent > getAppender(String name) {
    return aai.getAppender(name);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#isAttached(ch.qos.logback.core.Appender)
   */
  @Override
  public boolean isAttached(Appender<ILoggingEvent> appender) {
    return aai.isAttached(appender);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#detachAndStopAllAppenders()
   */
  @Override
  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#detachAppender(ch.qos.logback.core.Appender)
   */
  @Override
  public boolean detachAppender(Appender<ILoggingEvent> appender) {
    return aai.detachAppender(appender);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.qos.logback.core.spi.AppenderAttachable#detachAppender(java.lang.String)
   */
  @Override
  public boolean detachAppender(String name) {
    return aai.detachAppender(name);
  }

}
