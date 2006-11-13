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
 * currently written to. If it grows bigger than the specified size, 
 * the FileAppender using the SizeBasedTriggeringPolicy rolls the file
 * and creates a new one.
 * <p>
 * Here is an example of a configuration using SizeBasedTriggeringPolicy.
 * <p>
 * <pre>
 * &lt;configuration&gt;
 *    &lt;appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *    &lt;rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy"&gt;
 *      &lt;param name="ActiveFileName" value="outputFile.log" /&gt;
 *      &lt;param name="FileNamePattern" value="logFile.%i.log" /&gt;
 *      &lt;param name="MinIndex" value="1" /&gt;
 *      &lt;param name="MaxIndex" value="3" /&gt;
 *    &lt;/rollingPolicy&gt;
 *
 *    <b>&lt;triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy"&gt;
 *      &lt;param name="MaxFileSize" value="5MB" /&gt;
 *    &lt;/triggeringPolicy&gt;</b>
 *
 *    &lt;layout class="ch.qos.logback.classic.PatternLayout"&gt;
 *      &lt;param name="pattern" value="%-4relative [%thread] %-5level %class - %msg%n" /&gt;
 *    &lt;/layout&gt;
 *  &lt;/appender&gt; 
 * 
 *  &lt;root&gt;
 *    &lt;level value="debug" /&gt;
 *    &lt;appender-ref ref="FILE" /&gt;
 *  &lt;/root&gt;
 * &lt;/configuration&gt;
 * </pre>
 * 
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

  public boolean isTriggeringEvent(final File activeFile, final Object event) {
    //System.out.println("Size"+file.length());
    return (activeFile.length() >= maxFileSizeAsLong);
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
        addError("[" + s + "] is not in proper int form. For more info, please visit http://logback.qos.ch/codes.html#sbtp_size_format");
        addError("[" + value + "] not in expected format.", e);
      }
    }
    return DEFAULT_MAX_FILE_SIZE;
  } 
}
