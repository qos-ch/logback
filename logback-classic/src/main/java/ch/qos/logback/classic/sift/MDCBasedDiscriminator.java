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
package ch.qos.logback.classic.sift;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.sift.Discriminator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

/**
 * MDCBasedDiscriminator essentially returns the value mapped to an MDC key. If
 * the said value is null, then a default value is returned.
 * 
 * <p>Both Key and the DefaultValue are user specified properties.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class MDCBasedDiscriminator extends ContextAwareBase implements
    Discriminator<ILoggingEvent> {

  private String key;
  private String defaultValue;
  private boolean started = false;

  public MDCBasedDiscriminator() {
  }

  /**
   * Return the value associated with an MDC entry designated by the Key
   * property. If that value is null, then return the value assigned to the
   * DefaultValue property.
   */
  public String getDiscriminatingValue(ILoggingEvent event) {
    String mdcValue = MDC.get(key);
    if (mdcValue == null) {
      return defaultValue;
    } else {
      return mdcValue;
    }
  }

  public boolean isStarted() {
    return started;
  }

  public void start() {
    int errors = 0;
    if (OptionHelper.isEmpty(key)) {
      errors++;
      addError("The \"Key\" property must be set");
    }
    if (OptionHelper.isEmpty(defaultValue)) {
      errors++;
      addError("The \"DefaultValue\" property must be set");
    }
    if (errors == 0) {
      started = true;
    }
  }

  public void stop() {
    started = false;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @see #setDefaultValue(String)
   * @return
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * The default MDC value in case the MDC is not set for
   * {@link #setKey(String) mdcKey}.
   * 
   * <p> For example, if {@link #setKey(String) Key} is set to the value
   * "someKey", and the MDC is not set for "someKey", then this appender will
   * use the default value, which you can set with the help of this method.
   * 
   * @param defaultValue
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
