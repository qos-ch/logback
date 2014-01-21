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
package ch.qos.logback.classic.spi;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

/**
 * OSGi aware implementation of ClassPackagingDataProvider
 */
public class OSGIPackagingDataProvider implements ClassPackagingDataProvider {
  private final ClassPackagingDataProvider defaultProvider = new DefaultPackagingDataProvider();

  public String getImplementationVersion(Class type) {
    if(type == null){
      return defaultProvider.getImplementationVersion(type);
    }
    Bundle b = FrameworkUtil.getBundle(type);
    if(b == null){
      return defaultProvider.getImplementationVersion(type);
    }
    final Version version = b.getVersion();
    return version == org.osgi.framework.Version.emptyVersion ? "na" : version.toString();
  }

  public String getCodeLocation(Class type) {
    if(type == null){
      return defaultProvider.getCodeLocation(type);
    }

    Bundle b = FrameworkUtil.getBundle(type);
    if(b == null){
      return defaultProvider.getCodeLocation(type);
    }
    return b.getSymbolicName();
  }
}
