/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;

/**
 * Base class for SyslogAppender.
 * 
 * @author Ceki G&uumllc&uuml;
 *
 * @param <E>
 */
public abstract class SyslogAppenderBase<E> extends AppenderBase<E> {

  final static String SYSLOG_LAYOUT_URL = CoreConstants.CODES_URL + "#syslog_layout";
  final static int MSG_SIZE_LIMIT = 256*1024;
  
  Layout<E> layout;
  String facilityStr;
  String syslogHost;
  protected String suffixPattern;
  SyslogWriter sw;
  int port = SyslogConstants.SYSLOG_PORT;

  public void start() {
    int errorCount = 0;
    if (facilityStr == null) {
      addError("The Facility option is mandatory");
      errorCount++;
    }

    try {
      sw = new SyslogWriter(syslogHost, port);
    } catch (UnknownHostException e) {
      addError("Could not create SyslogWriter", e);
      errorCount++;
    } catch (SocketException e) {
      errorCount++;
      addError("Failed to bind to a random datagram socket ", e);
    }

    if (layout == null) {
      layout = buildLayout(facilityStr);
    }

    if (errorCount == 0) {
      super.start();
    }
  }
  
  abstract public Layout<E> buildLayout(String facilityStr);

  abstract public int getSeverityForEvent(Object eventObject);

  @Override
  protected void append(E eventObject) {
    if (!isStarted()) {
      return;
    }

    try {
      String msg = layout.doLayout(eventObject);
      if(msg != null && msg.length() > MSG_SIZE_LIMIT) {
        msg = msg.substring(0, MSG_SIZE_LIMIT);
      }
      sw.write(msg);
      sw.flush();
      postProcess(eventObject, sw);
    } catch (IOException ioe) {
      addError("Failed to send diagram to " + syslogHost, ioe);
      stop();
    }
  }
  
  protected void postProcess(Object event, SyslogWriter sw) {
    
  }

  /**
   * Returns the integer value corresponding to the named syslog facility.
   * 
   * @throws IllegalArgumentException
   *           if the facility string is not recognized
   */
  static public int facilityStringToint(String facilityStr) {
    if ("KERN".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_KERN;
    } else if ("USER".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_USER;
    } else if ("MAIL".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_MAIL;
    } else if ("DAEMON".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_DAEMON;
    } else if ("AUTH".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_AUTH;
    } else if ("SYSLOG".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_SYSLOG;
    } else if ("LPR".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LPR;
    } else if ("NEWS".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_NEWS;
    } else if ("UUCP".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_UUCP;
    } else if ("CRON".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_CRON;
    } else if ("AUTHPRIV".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_AUTHPRIV;
    } else if ("FTP".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_FTP;
    } else if ("LOCAL0".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL0;
    } else if ("LOCAL1".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL1;
    } else if ("LOCAL2".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL2;
    } else if ("LOCAL3".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL3;
    } else if ("LOCAL4".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL4;
    } else if ("LOCAL5".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL5;
    } else if ("LOCAL6".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL6;
    } else if ("LOCAL7".equalsIgnoreCase(facilityStr)) {
      return SyslogConstants.LOG_LOCAL7;
    } else {
      throw new IllegalArgumentException(facilityStr
          + " is not a valid syslog facility string");
    }
  }

  /**
   * Returns the value of the <b>SyslogHost</b> option.
   */
  public String getSyslogHost() {
    return syslogHost;
  }

  /**
   * The <b>SyslogHost</b> option is the name of the the syslog host where log
   * output should go.
   * 
   * <b>WARNING</b> If the SyslogHost is not set, then this appender will fail.
   */
  public void setSyslogHost(String syslogHost) {
    this.syslogHost = syslogHost;
  }

  /**
   * Returns the string value of the <b>Facility</b> option.
   * 
   * See {@link #setFacility} for the set of allowed values.
   */
  public String getFacility() {
    return facilityStr;
  }

  /**
   * The <b>Facility</b> option must be set one of the strings KERN, USER,
   * MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, NTP,
   * AUDIT, ALERT, CLOCK, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5,
   * LOCAL6, LOCAL7. Case is not important.
   * 
   * <p>
   * See {@link SyslogConstants} and RFC 3164 for more information about the
   * <b>Facility</b> option.
   */
  public void setFacility(String facilityStr) {
    if (facilityStr != null) {
      facilityStr = facilityStr.trim();
    }
    this.facilityStr = facilityStr;
  }

  /**
   * 
   * @return
   */
  public int getPort() {
    return port;
  }

  /**
   * The port number on the syslog server to connect to. Nornally, wou would not
   * want to change the default value, that is 514.
   */
  public void setPort(int port) {
    this.port = port;
  }


  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    addWarn("The layout of a SyslogAppender cannot be set directly. See also "+SYSLOG_LAYOUT_URL);
  }

  @Override
  public void stop() {
    sw.close();
    super.stop();
  }

  /**
   * See {@link #setSuffixPattern(String).
   * 
   * @return
   */
  public String getSuffixPattern() {
    return suffixPattern;
  }

  /**
   * The <b>suffixPattern</b> option specifies the fortmat of the
   * non-standardized part the message sent to the syslog server.
   * 
   * @param pattern
   */
  public void setSuffixPattern(String suffixPattern) {
    this.suffixPattern = suffixPattern;
  }
}
