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
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;

/**
 * Base class for SyslogAppender.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E>
 */
public abstract class SyslogAppenderBase<E> extends AppenderBase<E> {

  final static String SYSLOG_LAYOUT_URL = CoreConstants.CODES_URL
      + "#syslog_layout";

  final static int MSG_SIZE_LIMIT = 256 * 1024;
  public static final int APPNAME_MAXCHARS = 48;
  public static final int MESSAGEID_MAXCHARS = 32;
  public static final int STRUCTUREDDATAID_MAXCHARS = 32;
  public static final String STRUCTUREDDATAKEYS_DELIM = ",";
  public static final int SDNAME_MAXCHARS = 32;

  Layout<E> layout;
  String facilityStr;
  boolean rfc5424 = false;
  String appName;
  String messageId;
  String structuredDataId;
  List<String> structuredDataKeyList = new ArrayList<String>();
  boolean messageIdInSuffix = false;
  String syslogHost;
  protected String suffixPattern;
  SyslogOutputStream sos;
  int port = SyslogConstants.SYSLOG_PORT;
  int maxMessageSize = 65000;

  @Override
  public void start() {
    int errorCount = 0;
    if (facilityStr == null) {
      addError("The Facility option is mandatory");
      errorCount++;
    }

    try {
      sos = new SyslogOutputStream(syslogHost, port);
    } catch (UnknownHostException e) {
      addError("Could not create SyslogWriter", e);
      errorCount++;
    } catch (SocketException e) {
      addWarn(
          "Failed to bind to a random datagram socket. Will try to reconnect later.",
          e);
    }

    if (layout == null) {
      layout = buildLayout();
    }

    if (errorCount == 0) {
      super.start();
    }
  }

  abstract public Layout<E> buildLayout();

  abstract public int getSeverityForEvent(Object eventObject);

  @Override
  protected void append(E eventObject) {
    if (!isStarted()) {
      return;
    }

    try {
      String msg = layout.doLayout(eventObject);
      if(msg == null) {
        return;
      }
      if (msg.length() > maxMessageSize) {
        msg = msg.substring(0, maxMessageSize);
      }
      sos.write(msg.getBytes());
      sos.flush();
      postProcess(eventObject, sos);
    } catch (IOException ioe) {
      addError("Failed to send diagram to " + syslogHost, ioe);
    }
  }

  protected void postProcess(Object event, OutputStream sw) {

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
   * The <b>Facility</b> option must be set one of the strings KERN, USER, MAIL,
   * DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, NTP, AUDIT,
   * ALERT, CLOCK, LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6,
   * LOCAL7. Case is not important.
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
   * Returns the boolean value of <b>Rfc5424</b> option.
   * See {@link #setRfc5424}.
   */
  public boolean isRfc5424() {
    return rfc5424;
  }

  /**
   * The <b>Rfc5424</b> option when "true" will format the syslog message
   * in compliance with <a href="http://tools.ietf.org/html/rfc5424">RFC5424</a>.
   * Otherwise, the legacy <a href="http://tools.ietf.org/html/rfc3164>RFC3164</a>
   * format is used.
   * @param rfc5424
   */
  public void setRfc5424(boolean rfc5424) {
    this.rfc5424 = rfc5424;
  }

  /**
   * Returns the string value of <b>AppName</b> option.
   */
  public String getAppName() {
    return appName;
  }

  /**
   * The <b>AppName</b> option is only used when the <b>Rfc5424</b> option is
   * enabled. This is intended to identify the application source of the
   * log message, limited to 48 characters.
   * See {@link #setRfc5424}.
   * @param appName
   */
  public void setAppName(String appName) {
    if (appName == null || appName.length() == 0) {
      throw new IllegalArgumentException("Null or empty <appName> property");
    }
    if (APPNAME_MAXCHARS < appName.length()) {
      throw new IllegalArgumentException("<appName> length limited to " + APPNAME_MAXCHARS);
    }
    this.appName = appName;
  }

  /**
   * Returns the string value of the <b>MesageId</b> option.
   */
  public String getMessageId() {
    return messageId;
  }

  /**
   * The <b>MesageId</b> option is only used when the <b>Rfc5424</b> option is
   * enabled. This is intended to identify the type of the
   * log message, limited to 32 characters.
   * See {@link #setRfc5424}.
   */
  public void setMessageId(String messageId) {
    if (messageId == null || messageId.length() == 0) {
      throw new IllegalArgumentException("Null or empty <messageId> property");
    }
    if (MESSAGEID_MAXCHARS < messageId.length()) {
      throw new IllegalArgumentException("<messageId> length limited to " + MESSAGEID_MAXCHARS);
    }
    this.messageId = messageId;
  }

  /**
   * Returns the string value of <b>StructuredDataId</b> option.
   */
  public String getStructuredDataId() {
    return structuredDataId;
  }

  /**
   * The <b>StructuredDataId</b> option is only used when the <b>Rfc5424</b> 
   * option is enabled. When this option is set it used as the structured data id
   * and key/value pairs of the MDC data are placed into the log message
   * as structured data parameters. If the <b>StructuredDataKey</b> option 
   * is not specified then all MDC data is output. Id is limited to 32 characters.
   * See {@link #setRfc5424}.
   */
  public void setStructuredDataId(String structuredDataId) {
    if (structuredDataId == null || structuredDataId.length() == 0) {
      throw new IllegalArgumentException("Null or empty <structuredDataId> property");
    }
    if (STRUCTUREDDATAID_MAXCHARS < structuredDataId.length()) {
      throw new IllegalArgumentException("<structuredDataId> length limited to " + STRUCTUREDDATAID_MAXCHARS);
    }
    this.structuredDataId = structuredDataId;
  }
  
  public List<String> getStructuredDataKeysAsListOfStrings() {
    return structuredDataKeyList;
  }
  
  public String getStructuredDataKeys() {
    StringBuilder sb = new StringBuilder();
    for (String key : structuredDataKeyList) {
      if (0 < sb.length()) {
        sb.append(STRUCTUREDDATAKEYS_DELIM);
      }
      sb.append(key);
    }
    return sb.toString();
  }
  
  /**
   * The <b>StructuredDataKeys</b> option is only used when the <b>Rfc5424</b> 
   * option is enabled. This specifies which keys from the MDC are output
   * in the structured data. Multiple keys can be specified by separating the 
   * keys with commas. Keys are limited to 32 characters.
   */
  public void setStructuredDataKeys(String structuredDataKeys) {
    if (structuredDataKeys == null || structuredDataKeys.length() == 0) {
      throw new IllegalArgumentException("Null or empty <structuredDataKeys> property");
    }
    String[] keys = structuredDataKeys.split(STRUCTUREDDATAKEYS_DELIM);
    for (String key : keys) {
      key = key.trim();
      if (SDNAME_MAXCHARS < key.length()) {
        throw new IllegalArgumentException("<structuredDataKeys> key length limited to " + SDNAME_MAXCHARS);
      }
      structuredDataKeyList.add(key);
    }
  }
  
  /**
   * Returns the boolean value of <b>MessageIdInSuffix</b> option.
   */
  public boolean isMessageIdInSuffix() {
    return messageIdInSuffix;
  }
  
  /**
   * The <b>MessageIdInSuffix</b> option is only used when the <b>Rfc5424</b> 
   * option is enabled. This specifies that the message id and structured data
   * portions of the syslog message will be supplied in the suffix. This is
   * convenient if the suffix is set to "%msg" and the program places the
   * message id and structured data at the start of the message. It provides
   * the program more flexibility with the structured data.Note that
   * the suffix must follow the RFC5424 standard with respect to formatting.
   * See SyslogStartConverter for formatting helper methods.
   */
  public void setMessageIdInSuffix(boolean messageIdInSuffix) {
    this.messageIdInSuffix = messageIdInSuffix;
  }

  /**
   * 
   * @return
   */
  public int getPort() {
    return port;
  }

  /**
   * The port number on the syslog server to connect to. Normally, you would not
   * want to change the default value, that is 514.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * 
   * @return
   */
  public int getMaxMessageSize() {
    return maxMessageSize;
  }

  /**
   * Maximum size for the syslog message (in characters); messages
   * longer than this are truncated. The default value is 65400 (which
   * is near the maximum for syslog-over-UDP). Note that the value is
   * characters; the number of bytes may vary if non-ASCII characters
   * are present.
   */
  public void setMaxMessageSize(int maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  public Layout<E> getLayout() {
    return layout;
  }

  public void setLayout(Layout<E> layout) {
    addWarn("The layout of a SyslogAppender cannot be set directly. See also "
        + SYSLOG_LAYOUT_URL);
  }

  @Override
  public void stop() {
    sos.close();
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
   * The <b>suffixPattern</b> option specifies the format of the
   * non-standardized part of the message sent to the syslog server.
   * 
   * @param suffixPattern
   */
  public void setSuffixPattern(String suffixPattern) {
    this.suffixPattern = suffixPattern;
  }
}
