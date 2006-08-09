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
  
  long maxFileSize = DEFAULT_MAX_FILE_SIZE; 

  public SizeBasedTriggeringPolicy() {
  }

  public SizeBasedTriggeringPolicy(final long maxFileSize) {
      this.maxFileSize = maxFileSize;
  }


  public boolean isTriggeringEvent(final File file, final Object event) {
    //System.out.println("Size"+file.length());
    return (file.length() >= maxFileSize);
  }

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(long l) {
    maxFileSize = l;
  }
  
}
