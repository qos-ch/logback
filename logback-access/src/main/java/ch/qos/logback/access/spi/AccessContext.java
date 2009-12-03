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
package ch.qos.logback.access.spi;

import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A minimal context implementation used by certain logback-access components,
 * mainly SocketServer.
 * 
 * @author S&eacute;bastien Pennec
 */
public class AccessContext extends ContextBase implements
    AppenderAttachable<AccessEvent>, FilterAttachable<AccessEvent> {

  AppenderAttachableImpl<AccessEvent> aai = new AppenderAttachableImpl<AccessEvent>();
  FilterAttachableImpl<AccessEvent> fai = new FilterAttachableImpl<AccessEvent>();

  public void callAppenders(AccessEvent event) {
    aai.appendLoopOnAppenders(event);
  }

  public void addAppender(Appender<AccessEvent> newAppender) {
    aai.addAppender(newAppender);
  }

  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();
  }

  public boolean detachAppender(Appender<AccessEvent> appender) {
    return aai.detachAppender(appender);
  }

  public boolean detachAppender(String name) {
    return aai.detachAppender(name);
  }

  public Appender<AccessEvent> getAppender(String name) {
    return aai.getAppender(name);
  }

  public boolean isAttached(Appender<AccessEvent> appender) {
    return aai.isAttached(appender);
  }

  public Iterator<Appender<AccessEvent>> iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public void addFilter(Filter<AccessEvent> newFilter) {
    fai.addFilter(newFilter);
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public List<Filter<AccessEvent>> getCopyOfAttachedFiltersList() {
    return fai.getCopyOfAttachedFiltersList();
  }

  public FilterReply getFilterChainDecision(AccessEvent event) {
    return fai.getFilterChainDecision(event);
  }

  public Filter<AccessEvent> getFirstFilter() {
    return fai.getFirstFilter();
  }
}
