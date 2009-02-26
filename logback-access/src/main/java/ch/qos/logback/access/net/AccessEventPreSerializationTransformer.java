/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.access.net;

import java.io.Serializable;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class AccessEventPreSerializationTransformer implements
    PreSerializationTransformer<AccessEvent> {

  public Serializable transform(AccessEvent event) {
    return event;
  }

}
