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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.qos.logback.classic.net.testObjectBuilders.LoggingEventBuilderInContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.read.ListAppender;


/**
 * Unit tests associated to {@link DelegateAppender}.
 *
 * @author Juan Pablo Santos Rodríguez
 */
public class DelegateAppenderTest {

  @Test
  public void testDelegatesToChildAppendersOnDoAppend() {
    // given
    LoggerContext lc = new LoggerContext();

    ListAppender<ILoggingEvent> listApp1 = new ListAppender<ILoggingEvent>();
    listApp1.setContext(lc);
    listApp1.setName("list1");
    listApp1.start();

    ListAppender<ILoggingEvent> listApp2 = new ListAppender<ILoggingEvent>();
    listApp2.setContext(lc);
    listApp2.setName("list2");
    listApp2.start();

    DelegateAppender delegateApp = new DelegateAppender();
    delegateApp.addAppender(listApp1);
    delegateApp.addAppender(listApp2);
    delegateApp.setContext(lc);
    delegateApp.setName("delegate");
    delegateApp.start();

    LoggingEventBuilderInContext builder = new LoggingEventBuilderInContext(lc, DelegateAppenderTest.class.getName(), UnsynchronizedAppenderBase.class.getName());

    // when
    delegateApp.doAppend(builder.build(66));
    delegateApp.stop();

    // then
    assertEquals(1, listApp1.list.size());
    assertEquals(1, listApp2.list.size());
  }

}
