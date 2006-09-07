/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling;

import java.io.File;

/**
 * SizeBasedTriggeringPolicy looks at size of the file being
 * currently written to.
 * 
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class SizeBasedTriggeringPolicy extends TriggeringPolicyBase {
  
  /**
   * The default maximum file size.
   */
  public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  
  String maxFileSize = Long.toString(DEFAULT_MAX_FILE_SIZE); 
  long maxFileSizeAsLong;

  public SizeBasedTriggeringPolicy() {
  }

  public SizeBasedTriggeringPolicy(final String maxFileSize) {
      setMaxFileSize(maxFileSize);
  }

  public boolean isTriggeringEvent(final File file, final Object event) {
    //System.out.println("Size"+file.length());
    return (file.length() >= maxFileSizeAsLong);
  }

  public String getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(String maxFileSize) {
    this.maxFileSize = maxFileSize;
    this.maxFileSizeAsLong = toFileSize(maxFileSize);
  }
  
  long toFileSize(String value) {
    if(value == null)
      return DEFAULT_MAX_FILE_SIZE;

    String s = value.trim().toUpperCase();
    long multiplier = 1;
    int index;

    if((index = s.indexOf("KB")) != -1) {
      multiplier = 1024;
      s = s.substring(0, index);
    }
    else if((index = s.indexOf("MB")) != -1) {
      multiplier = 1024*1024;
      s = s.substring(0, index);
    }
    else if((index = s.indexOf("GB")) != -1) {
      multiplier = 1024*1024*1024;
      s = s.substring(0, index);
    }
    if(s != null) {
      try {
        return Long.valueOf(s).longValue() * multiplier;
      }
      catch (NumberFormatException e) {
        addError("[" + s + "] is not in proper int form.");
        addError("[" + value + "] not in expected format.", e);
      }
    }
    return DEFAULT_MAX_FILE_SIZE;
  } 
}
