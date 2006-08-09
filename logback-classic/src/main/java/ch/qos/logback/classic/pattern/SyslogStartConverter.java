/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.pattern;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.net.SyslogAppenderBase;

public class SyslogStartConverter extends ClassicConverter {

  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat simpleFormat;
  String localHostName;
  int facility;

  public void start() {
    int errorCount = 0;
    
    String facilityStr = getFirstOption();
    if (facilityStr == null) {
      //errorCount++;
      addError("was expecting a facility string as an option");
      return;
    }

    facility = SyslogAppenderBase.facilityStringToint(facilityStr);
    localHostName = getLocalHostname();
    try {
      simpleFormat = new SimpleDateFormat("MMM dd hh:mm:ss", new DateFormatSymbols(Locale.US));
    } catch (IllegalArgumentException e) {
      addError("Could not instantiate SimpleDateFormat", e);
      errorCount++;
    }

    if(errorCount == 0) {
      super.start();
    }
  }

  public String convert(Object event) {
    LoggingEvent le = (LoggingEvent) event;
    StringBuilder sb = new StringBuilder();

    int pri = facility + LevelToSyslogSeverity.convert(le);
    System.out.println("" + pri);
    sb.append("<");
    sb.append(pri);
    sb.append(">");
    fillInTimestamp(sb, le.getTimeStamp());
    sb.append(' ');
    sb.append(localHostName);
    sb.append(' ');

    return sb.toString();
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

  void fillInTimestamp(StringBuilder sb, long timestamp) {
    // if called multiple times within the same millisecond
    // use last value
    if (timestamp != lastTimestamp) {
      lastTimestamp = timestamp;
      timesmapStr = simpleFormat.format(new Date(timestamp));
    }
    sb.append(timesmapStr);
  }
}
