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
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.spi.Interpreter;

public abstract class SiftingJoranConfiguratorBase<E> extends GenericConfigurator {

  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    NestedComplexPropertyIA nestedComplexIA = new NestedComplexPropertyIA();
    nestedComplexIA.setContext(context);
    interpreter.addImplicitAction(nestedComplexIA);
    
    NestedBasicPropertyIA nestedSimpleIA = new NestedBasicPropertyIA();
    nestedSimpleIA.setContext(context);
    interpreter.addImplicitAction(nestedSimpleIA);
  }
  
  abstract public Appender<E> getAppender();
}
