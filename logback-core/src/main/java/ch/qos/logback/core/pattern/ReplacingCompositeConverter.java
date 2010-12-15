/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.pattern;

import java.util.List;

public class ReplacingCompositeConverter<E> extends CompositeConverter<E> {

  String regex;
  String by;

  public void start() {
     final List<String> optionList = getOptionList();
     regex = optionList.get(0);
  }
  @Override
  String transform(String in) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
