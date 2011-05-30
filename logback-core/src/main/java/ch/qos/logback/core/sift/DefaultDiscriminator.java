/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.sift.Discriminator;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class DefaultDiscriminator<E> implements Discriminator<E> {

  static public final String DEFAULT = "default";

  boolean started = false;

  public String getDiscriminatingValue(E e) {
    return DEFAULT;
  }

  public String getKey() {
    return DEFAULT;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;  
  }
}
