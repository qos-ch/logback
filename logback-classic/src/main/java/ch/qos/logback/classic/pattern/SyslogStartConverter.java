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
package ch.qos.logback.classic.pattern;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.net.SyslogAppenderBase;

public class SyslogStartConverter extends ClassicConverter {
  public static final String RFC5424_NILVALUE = "-";
  public static final String TIMESTAMP_FORMAT_NAME_DEFAULT = "simple";
  public static final String TIMESTAMP_FORMAT_RFC3164 = "MMM dd HH:mm:ss";
  public static final String TIMESTAMP_FORMAT_RFC5424 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  public static final int SDNAME_MAXCHARS = 32;
  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat timestampFormat;
  String localHostName;
  int facility;
  boolean rfc5424 = false;
  String appName;
  String messageId;
  String structuredDataId;
  Pid pid;
  
  public static class Pid {
    private String pid;
    public Pid() {
      // try to obtain from system property
      pid = System.getProperty("pid");
      if (pid != null) {
        pid = pid.trim();
        if (pid.isEmpty()) {
          pid = null;
        }
      }
      if (pid == null) {
        try {
          // try to obtain by exec'ing shell
          byte[] bo = new byte[100];
          String[] cmd = {"bash", "-c", "echo $PPID"};
          Process p = Runtime.getRuntime().exec(cmd);
          int len = p.getInputStream().read(bo);
          pid = new String(bo, 0, len).trim();
          if (pid.isEmpty()) {
            pid = null;
          }
        } catch (IOException e) {
        }
      }
    }
    @Override
    public String toString() {
      return pid == null ? RFC5424_NILVALUE : pid;
    }
  }

  @Override
  public void start() {
    int errorCount = 0;
    
    String facilityStr = getFirstOption();
    if (facilityStr == null) {
      addError("was expecting a facility string as an option");
      return;
    }
    facility = SyslogAppenderBase.facilityStringToint(facilityStr);
    setupRFC5424();
    try {
      String format = rfc5424 ? TIMESTAMP_FORMAT_RFC5424 : TIMESTAMP_FORMAT_RFC3164;
      timestampFormat = new SimpleDateFormat(format, new DateFormatSymbols(Locale.US));
    } catch (IllegalArgumentException e) {
      addError("Could not instantiate SimpleDateFormat", e);
      errorCount++;
    }
    localHostName = getLocalHostname();

    if(errorCount == 0) {
      super.start();
    }
  }
  
  private void setupRFC5424() {
    if (getOptionList().size() < 2) {
      return;
    }
    String s = getOptionList().get(1);
    if (s == null) {
      return;
    }
    s = s.trim().toLowerCase();
    if (s.isEmpty()) {
      return;
    }
    if (s.equals("true") || s.equals("1")) {
      rfc5424 = true;
      appName = getRFC5424Option(2, SyslogAppenderBase.APPNAME_MAXCHARS);
      messageId = getRFC5424Option(3, SyslogAppenderBase.MSGID_MAXCHARS);
      structuredDataId = getRFC5424Option(4, SyslogAppenderBase.STRUCTUREDDATAID_MAXCHARS);
      pid = new Pid();
    }
  }
  
  private String getRFC5424Option(int i, int maxChars) {
    if (i < getOptionList().size()) {
      String s = getOptionList().get(i);
      if (s != null) {
        s = s.trim();
        if (!s.isEmpty()) {
          return s.substring(0, Math.min(s.length(), maxChars));
        }
      }
    }
    return RFC5424_NILVALUE;
  }

  @Override
  public String convert(ILoggingEvent event) {
    StringBuilder sb = new StringBuilder();
    int pri = facility + LevelToSyslogSeverity.convert(event);
    sb.append("<");
    sb.append(pri);
    sb.append(">");
    if (rfc5424) {
      sb.append("1 "); // version
      sb.append(computeTimeStampString(event.getTimeStamp()));
      sb.append(' ');
      sb.append(localHostName);
      sb.append(' ');
      sb.append(appName);
      sb.append(' ');
      sb.append(pid);
      sb.append(' ');
      sb.append(messageId);
      sb.append(' ');
      convertStructuredData(event, sb);
      sb.append(' ');
    } else {
      sb.append(computeTimeStampString(event.getTimeStamp()));
      sb.append(' ');
      sb.append(localHostName);
      sb.append(' ');
    }
    return sb.toString();
  }
  
  public void convertStructuredData(ILoggingEvent event, StringBuilder sb) {
    if (structuredDataId.equals(RFC5424_NILVALUE)) {
      sb.append(RFC5424_NILVALUE);
      return;
    }
    // converts all key/values in MDC map to structured data string.
    Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
    if (mdcPropertyMap == null || mdcPropertyMap.isEmpty()) {
      sb.append(RFC5424_NILVALUE);
      return;
    }
    sb.append('[');
    sb.append(structuredDataId);
    for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
      sb.append(' ');
      String s = entry.getKey();
      sb.append(s.substring(0, Math.min(s.length(), SDNAME_MAXCHARS)));
      sb.append("=\"");
      convertParamValue(entry.getValue(), sb);
      sb.append('"');
    }
    sb.append(']');
  }
  
  public static void convertParamValue(String value, StringBuilder sb) {
    for (int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i);
      if (c == '\\' || c == '"' || c == ']') {
        sb.append('\\');
      }
      sb.append(c);
    }
  }

  /**
   * This method gets the network name of the machine we are running on.
   * Returns "UNKNOWN_LOCALHOST" in the unlikely case where the host name 
   * cannot be found.
   * @return String the name of the local host
   */
  public String getLocalHostname() {
    try {
      InetAddress addr = InetAddress.getLocalHost();
      return addr.getHostName();
    } catch (UnknownHostException uhe) {
      addError("Could not determine local host name", uhe);
      return "UNKNOWN_LOCALHOST";
    }
  }

  String computeTimeStampString(long now) {
    synchronized (this) {
      if (now != lastTimestamp) {
        lastTimestamp = now;
        timesmapStr = timestampFormat.format(new Date(now));
      }
      return timesmapStr;
    }
  }  
}
