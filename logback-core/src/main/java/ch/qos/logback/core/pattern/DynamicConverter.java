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
package ch.qos.logback.core.pattern;

import java.util.List;

import ch.qos.logback.core.spi.LifeCycle;

abstract public class DynamicConverter<E> extends FormattingConverter<E>
    implements LifeCycle {

  // Contains a list of option Strings.
  private List optionList;

  /**
   * Is this component active?
   */
  boolean started = false;

  /**
   * Components that depend on options passed during configuration can override
   * this method in order to make appropriate use of those options. For simpler
   * components, the trivial implementation found in this abstract class will be
   * sufficient.
   */
  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }

  public void setOptionList(List optionList) {
    this.optionList = optionList;
  }

  /**
   * Return the first option passed to this component. The returned value may be
   * null if there are no options.
   * 
   * @return First option, may be null.
   */
  protected String getFirstOption() {
    if (optionList == null || optionList.size() == 0) {
      return null;
    } else {
      return (String) optionList.get(0);
    }
  }

  protected List getOptionList() {
    return optionList;
  }
}
