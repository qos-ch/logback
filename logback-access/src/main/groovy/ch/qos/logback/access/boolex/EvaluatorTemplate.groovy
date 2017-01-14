/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.boolex

import ch.qos.logback.access.spi.IAccessEvent
import ch.qos.logback.core.boolex.IEvaluator

// WARNING
// If this file is renamed, this should be reflected in
// logback-classic/pom.xml  resources section.

public class EvaluatorTemplate implements IEvaluator<IAccessEvent> {

  boolean doEvaluate(IAccessEvent event) {
    IAccessEvent e = event;
    //EXPRESSION
  }

}
