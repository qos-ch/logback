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
package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class ContextNameAction extends Action {

  public void begin(InterpretationContext ec, String name, Attributes attributes) {
  }

  public void body(InterpretationContext ec, String body) {

    String finalBody = ec.subst(body);
    addInfo("Setting logger context name as [" + finalBody + "]");
    try {
      context.setName(finalBody);
    } catch (IllegalStateException e) {
      addError("Failed to rename context [" + context.getName() + "] as ["
          + finalBody + "]", e);
    }
  }

  public void end(InterpretationContext ec, String name) {
  }
}
