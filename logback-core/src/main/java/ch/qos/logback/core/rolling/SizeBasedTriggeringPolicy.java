/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.rolling;

import java.io.File;

import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.InvocationGate;

/**
 * SizeBasedTriggeringPolicy looks at size of the file being currently written
 * to. If it grows bigger than the specified size, the FileAppender using the
 * SizeBasedTriggeringPolicy rolls the file and creates a new one.
 * 
 * For more information about this policy, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SizeBasedTriggeringPolicy
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class SizeBasedTriggeringPolicy<E> extends TriggeringPolicyBase<E> {

  public static final String SEE_SIZE_FORMAT = "http://logback.qos.ch/codes.html#sbtp_size_format";
  /**
   * The default maximum file size.
   */
  public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

  String maxFileSizeAsString = Long.toString(DEFAULT_MAX_FILE_SIZE);
  FileSize maxFileSize;

  public SizeBasedTriggeringPolicy() {
  }

  public SizeBasedTriggeringPolicy(final String maxFileSize) {
    setMaxFileSize(maxFileSize);
  }

  private InvocationGate invocationGate = new InvocationGate();

  public boolean isTriggeringEvent(final File activeFile, final E event) {
  if(invocationGate.skipFurtherWork())
      return false;

    long now = System.currentTimeMillis();
    invocationGate.updateMaskIfNecessary(now);

    return (activeFile.length() >= maxFileSize.getSize());
  }

  public String getMaxFileSize() {
    return maxFileSizeAsString;
  }

  public void setMaxFileSize(String maxFileSize) {
    this.maxFileSizeAsString = maxFileSize;
    this.maxFileSize = FileSize.valueOf(maxFileSize);
  }

  long toFileSize(String value) {
    if (value == null)
      return DEFAULT_MAX_FILE_SIZE;

    String s = value.trim().toUpperCase();
    long multiplier = 1;
    int index;

    if ((index = s.indexOf("KB")) != -1) {
      multiplier = 1024;
      s = s.substring(0, index);
    } else if ((index = s.indexOf("MB")) != -1) {
      multiplier = 1024 * 1024;
      s = s.substring(0, index);
    } else if ((index = s.indexOf("GB")) != -1) {
      multiplier = 1024 * 1024 * 1024;
      s = s.substring(0, index);
    }
    if (s != null) {
      try {
        return Long.valueOf(s).longValue() * multiplier;
      } catch (NumberFormatException e) {
        addError("[" + s + "] is not in proper int format. Please refer to "
            + SEE_SIZE_FORMAT);
        addError("[" + value + "] not in expected format.", e);
      }
    }
    return DEFAULT_MAX_FILE_SIZE;
  }
}
