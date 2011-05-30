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
package ch.qos.logback.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ContextUtil extends ContextAwareBase {

  public ContextUtil(Context context) {
    setContext(context);
  }

  static String getLocalHostName() throws UnknownHostException {
    InetAddress localhost = InetAddress.getLocalHost();
    return localhost.getHostName();
  }

  /**
   * Add the local host's name as a property
   */
  public void addHostNameAsProperty() {
    try {
      String localhostName =  getLocalHostName();
      context.putProperty(CoreConstants.HOSTNAME_KEY, localhostName);
    } catch (UnknownHostException e) {
      addError("Failed to get local hostname", e);
    } catch (SecurityException e) {
      addError("Failed to get local hostname", e);
    }
  }
}
