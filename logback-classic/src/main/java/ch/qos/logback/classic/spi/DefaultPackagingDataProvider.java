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

import java.net.URL;
import java.security.CodeSource;

public class DefaultPackagingDataProvider implements ClassPackagingDataProvider {

  public String getImplementationVersion(Class type) {
    if (type == null) {
      return "na";
    }
    Package aPackage = type.getPackage();
    if (aPackage != null) {
      String v = aPackage.getImplementationVersion();
      if (v == null) {
        return "na";
      } else {
        return v;
      }
    }
    return "na";

  }

  public String getCodeLocation(Class type) {
    try {
      if (type != null) {
        // file:/C:/java/maven-2.0.8/repo/com/icegreen/greenmail/1.3/greenmail-1.3.jar
        CodeSource codeSource = type.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
          URL resource = codeSource.getLocation();
          if (resource != null) {
            String locationStr = resource.toString();
            // now lets remove all but the file name
            String result = getCodeLocation(locationStr, '/');
            if (result != null) {
              return result;
            }
            return getCodeLocation(locationStr, '\\');
          }
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return "na";
  }

  private String getCodeLocation(String locationStr, char separator) {
    int idx = locationStr.lastIndexOf(separator);
    if (isFolder(idx, locationStr)) {
      idx = locationStr.lastIndexOf(separator, idx - 1);
      return locationStr.substring(idx + 1);
    } else if (idx > 0) {
      return locationStr.substring(idx + 1);
    }
    return null;
  }

  private boolean isFolder(int idx, String text) {
    return (idx != -1 && idx + 1 == text.length());
  }

}
