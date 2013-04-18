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
package ch.qos.logback.classic.pattern;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.net.SyslogAppenderBase;

public class SyslogStartConverter extends ClassicConverter {
  public static final String RFC5424_NILVALUE = "-";
  public static final String TIMESTAMP_FORMAT_NAME_DEFAULT = "simple";
  public static final String TIMESTAMP_FORMAT_RFC3164 = "MMM dd HH:mm:ss";
  public static final String TIMESTAMP_FORMAT_RFC5424 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat timestampFormat;
  String localHostName;
  int facility;
  boolean rfc5424 = false;
  String appName;
  String messageId;
  String structuredDataId;
  String[] structuredDataKeys;
  boolean messageIdInSuffix;
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
      if (rfc5424) {
          timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      }
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
      appName = getRFC5424Option(2);
      messageId = getRFC5424Option(3);
      structuredDataId = getRFC5424Option(4);
      String keys = getRFC5424Option(5);
      if (!keys.equals(RFC5424_NILVALUE)) {
        StringTokenizer tokenizer = new StringTokenizer(keys, SyslogAppender.OPTION_SDKEY_DELIM);
        structuredDataKeys = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); ++i) {
          structuredDataKeys[i] = tokenizer.nextToken();
        }
      }
      s = getRFC5424Option(6);
      messageIdInSuffix = (s.equals("true") || s.equals("1"));
      pid = new Pid();
    }
  }
  
  private String getRFC5424Option(int i) {
    if (i < getOptionList().size()) {
      String s = getOptionList().get(i);
      if (s != null) {
        s = s.trim();
        if (!s.isEmpty() && !s.equals("" + null)) {
          return s;
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
      if (!messageIdInSuffix) {
          sb.append(messageId);
          sb.append(' ');
          convertEventStructuredData(event, sb);
          sb.append(' ');
      }
    } else {
      sb.append(computeTimeStampString(event.getTimeStamp()));
      sb.append(' ');
      sb.append(localHostName);
      sb.append(' ');
    }
    return sb.toString();
  }
  
  private void convertEventStructuredData(ILoggingEvent event, StringBuilder sb) {
    if (structuredDataId.equals(RFC5424_NILVALUE)) {
      sb.append(RFC5424_NILVALUE);
      return;
    }
    Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
    if ((mdcPropertyMap == null || mdcPropertyMap.isEmpty()) && structuredDataKeys == null) {
      sb.append(RFC5424_NILVALUE);
      return;
    }
    convertStructuredDataMap(sb, structuredDataId, mdcPropertyMap, structuredDataKeys);
  }
  
  public static void convertStructuredDataMap(StringBuilder sb, String id, Map<String, String> keyValues, String[] keys) {
      sb.append('[');
      id = id.substring(0, Math.min(id.length(), SyslogAppenderBase.STRUCTUREDDATAID_MAXCHARS));
      sb.append(id);
      if (keys == null) {
        // Converts all key/values in MDC map to structured data string.
        // Sort keys so output is in consistent order (easier for parsing).
        Set<String> keySet = keyValues.keySet();
        String[] sortedKeys = new String[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            sortedKeys[i++] = key;
        }
        Arrays.sort(sortedKeys);
        for (String key : sortedKeys) {
          convertParamKeyValue(sb, key, keyValues.get(key));
        }
      } else {
        // Convert configured key/values only, in configuration order.
        for (String key : keys) {
          String value = keyValues.get(key);
          if (value == null) {
            value = "";
          }
          convertParamKeyValue(sb, key, value);
        }
      }
      sb.append(']');
  }
  
  public static void convertStructuredDataArgs(StringBuilder sb, String id, String...keyValues) {
      sb.append('[');
      id = id.substring(0, Math.min(id.length(), SyslogAppenderBase.STRUCTUREDDATAID_MAXCHARS));
      sb.append(id);
      for (int i = 0; i < keyValues.length; i += 2) {
          convertParamKeyValue(sb, keyValues[i], keyValues[i + 1]);
      }
      sb.append(']');
  }
  
  public static void convertParamKeyValue(StringBuilder sb, String key, String value) {
    sb.append(' ');
    key = key.substring(0, Math.min(key.length(), SyslogAppenderBase.SDNAME_MAXCHARS));
    sb.append(key);
    sb.append("=\"");
    for (int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i);
      if (c == '\\' || c == '"' || c == ']') {
        sb.append('\\');
      }
      sb.append(c);
    }
    sb.append('"');
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
