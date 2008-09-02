/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Marker;

/**
 * 
 * @author James Strachan
 * @author Ceki Gulcu
 */
public class Util {

  static Map<String, PackageInfo> cache = new HashMap<String, PackageInfo>();

  static public boolean match(Marker marker, Marker[] markerArray) {
    if (markerArray == null) {
      throw new IllegalArgumentException("markerArray should not be null");
    }

    // System.out.println("event marker="+marker);

    final int size = markerArray.length;
    for (int i = 0; i < size; i++) {
      // System.out.println("other:"+markerArray[i]);

      if (marker.contains(markerArray[i])) {
        return true;
      }
    }
    return false;
  }

  static String getVersion(String className) {
    String packageName = getPackageName(className);
    Package aPackage = Package.getPackage(packageName);
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

  static public PackageInfo getPackageInfo(String className) {
    PackageInfo pi = cache.get(className);
    if(pi != null) {
      return pi;
    }
    String version = getVersion(className);
    String jarname = getJarNameOfClass(className);
    pi = new PackageInfo(jarname, version);
    //cache.put(className, pi);
    return pi;
  }

  static String getPackageName(String className) {
    int j = className.lastIndexOf('.');
    return className.substring(0, j);
  }

  /**
   * Uses the context class path or the current global class loader to deduce
   * the file that the given class name comes from
   */
  static String getJarNameOfClass(String className) {
    try {
      Class type = findClass(className);
      if (type != null) {
        URL resource = type.getClassLoader().getResource(
            type.getName().replace('.', '/') + ".class");
        // "jar:file:/C:/java/../repo/groupId/artifact/1.3/artifact-1.3.jar!/com/some/package/Some.class
        if (resource != null) {
          String text = resource.toString();
          int idx = text.lastIndexOf('!');
          if (idx > 0) {
            text = text.substring(0, idx);
            // now lets remove all but the file name
            idx = text.lastIndexOf('/');
            if (idx > 0) {
              text = text.substring(idx + 1);
            }
            idx = text.lastIndexOf('\\');
            if (idx > 0) {
              text = text.substring(idx + 1);
            }
            return text;
          }
        }
      }
    } catch (Exception e) {
      // ignore
    }
    return "na";
  }

  static private Class findClass(String className) {
    try {
      return Thread.currentThread().getContextClassLoader()
          .loadClass(className);
    } catch (ClassNotFoundException e) {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e1) {
        try {
          return Util.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e2) {
          return null;
        }
      }
    }
  }

}
