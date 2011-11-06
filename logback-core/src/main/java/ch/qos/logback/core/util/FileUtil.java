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

import ch.qos.logback.core.spi.ContextAware;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileUtil {


  public static URL fileToURL(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unexpected exception on file ["+file+"]", e);
    }
  }

  static public boolean isParentDirectoryCreationRequired(File file) {
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      return true;
    } else {
      return false;
    }
  }

  static public boolean createMissingParentDirectories(File file) {
    File parent = file.getParentFile();
    if (parent == null) {
      throw new IllegalStateException(file + " should not have a null parent");
    }
    if (parent.exists()) {
      throw new IllegalStateException(file + " should not have existing parent directory");
    }
    return parent.mkdirs();
  }


  static public String resourceAsString(ContextAware ca, ClassLoader classLoader, String resourceName) {
    URL url = classLoader.getResource(resourceName);
    if (url == null) {
      ca.addError("Failed to find resource [" + resourceName + "]");
      return null;
    }

    URLConnection urlConnection = null;
    try {
      urlConnection = url.openConnection();
      urlConnection.setUseCaches(false);
      InputStream is = urlConnection.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      char[] buf = new char[128];
      StringBuilder builder = new StringBuilder();
      int count = -1;
      while ((count = isr.read(buf, 0, buf.length)) != -1) {
        builder.append(buf, 0, count);
      }
      isr.close();
      is.close();
      return builder.toString();
    } catch (IOException e) {
      ca.addError("Failled to open " + resourceName, e);
    }
    return null;
  }
}
