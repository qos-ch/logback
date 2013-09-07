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
package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FileUtil extends ContextAwareBase {

  public FileUtil(Context context) {
    setContext(context);
  }

  public static URL fileToURL(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unexpected exception on file [" + file + "]", e);
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


  public String resourceAsString(ClassLoader classLoader, String resourceName) {
    URL url = classLoader.getResource(resourceName);
    if (url == null) {
      addError("Failed to find resource [" + resourceName + "]");
      return null;
    }

    InputStreamReader isr = null;
    try {
      URLConnection urlConnection = url.openConnection();
      urlConnection.setUseCaches(false);
      isr = new InputStreamReader(urlConnection.getInputStream());
      char[] buf = new char[128];
      StringBuilder builder = new StringBuilder();
      int count = -1;
      while ((count = isr.read(buf, 0, buf.length)) != -1) {
        builder.append(buf, 0, count);
      }
      return builder.toString();
    } catch (IOException e) {
      addError("Failed to open " + resourceName, e);
    } finally {
      if (isr != null) {
        try {
          isr.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
    return null;
  }

  static final int BUF_SIZE = 32 * 1024;

  public void copy(String src, String destination) throws RolloverFailure {
    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(src));
      bos = new BufferedOutputStream(new FileOutputStream(destination));
      byte[] inbuf = new byte[BUF_SIZE];
      int n;

      while ((n = bis.read(inbuf)) != -1) {
        bos.write(inbuf, 0, n);
      }

      bis.close();
      bis = null;
      bos.close();
      bos = null;
    } catch (IOException ioe) {
      String msg = "Failed to copy [" + src + "] to [" + destination + "]";
      addError(msg, ioe);
      throw new RolloverFailure(msg);
    } finally {
      if (bis != null) {
        try {
          bis.close();
        } catch (IOException e) {
          // ignore
        }
      }
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }
}
