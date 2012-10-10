/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.sift;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class SiftAction extends Action implements InPlayListener {
  private static final String TIMEOUT_ATTR = "timeout";
  private static final String MAX_APPENDERS_ATTR = "maxAppenders";

  List<SaxEvent> seList;

  @Override
  public void begin(InterpretationContext ic, String name, Attributes attributes)
      throws ActionException {
    Object o = ec.peekObject();
    if (o instanceof SiftingAppender) {
      SiftingAppender sa = (SiftingAppender) o;
      String timeoutAttr = attributes.getValue(TIMEOUT_ATTR);
      if (timeoutAttr != null && !timeoutAttr.isEmpty()) {
        int timeout = Integer.parseInt(timeoutAttr);
        sa.setTimeout(timeout);
        addInfo("Sub-appenders will timeout after " + timeout + " seconds");
      }
      String maxAppendersAttr = attributes.getValue(MAX_APPENDERS_ATTR);
      if (maxAppendersAttr != null && !maxAppendersAttr.isEmpty()) {
        int maxAppenders = Integer.parseInt(maxAppendersAttr);
        sa.setMaxAppenders(maxAppenders);
        addInfo("Will keep a maximum of " + maxAppenders + " sub-appenders");
      }
    }
    seList = new ArrayList<SaxEvent>();
    ic.addInPlayListener(this);
  }

  @Override
  public void end(InterpretationContext ic, String name) throws ActionException {
    ic.removeInPlayListener(this);
    Object o = ic.peekObject();
    if (o instanceof SiftingAppender) {
      SiftingAppender sa = (SiftingAppender) o;
      AppenderFactory appenderFactory = new AppenderFactory(seList, sa
          .getDiscriminatorKey());
      sa.setAppenderFactory(appenderFactory);
    }
  }

  public void inPlay(SaxEvent event) {
    seList.add(event);
  }

  public List<SaxEvent> getSeList() {
    return seList;
  }

}
