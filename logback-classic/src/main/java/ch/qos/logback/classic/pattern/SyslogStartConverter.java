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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.LevelToSyslogSeverity;
import ch.qos.logback.core.net.SyslogAppenderBase;

public class SyslogStartConverter extends ClassicConverter {

  long lastTimestamp = -1;
  String timesmapStr = null;
  SimpleDateFormat simpleFormat;
  String localHostName = null;
  int facility;

  public void start() {
    int errorCount = 0;
    
    String facilityStr = null;
    List optionList = getOptionList();    
    if (optionList != null) {
      if(optionList.size() > 0)
      {
        facilityStr = (String) optionList.get(0);
      }
      
      if(optionList.size() > 1)
      {
        localHostName = (String) optionList.get(1);      
      }     
    }
        
    if (facilityStr == null || facilityStr.equals("null")) {
      addError("was expecting a facility string as an option");
      return;
    }
    facility = SyslogAppenderBase.facilityStringToint(facilityStr);
        
    if (localHostName == null || localHostName.equals("null")) {      
      localHostName = getLocalHostname();
    }

    try {
      // hours should be in 0-23, see also http://jira.qos.ch/browse/LBCLASSIC-48
      simpleFormat = new SimpleDateFormat("MMM dd HH:mm:ss", new DateFormatSymbols(Locale.US));
    } catch (IllegalArgumentException e) {
      addError("Could not instantiate SimpleDateFormat", e);
      errorCount++;
    }

    if(errorCount == 0) {
      super.start();
    }
  }

  public String convert(ILoggingEvent event) {
    StringBuilder sb = new StringBuilder();

    int pri = facility + LevelToSyslogSeverity.convert(event);
  
    sb.append("<");
    sb.append(pri);
    sb.append(">");
    sb.append(computeTimeStampString(event.getTimeStamp()));
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

  String computeTimeStampString(long now) {
    synchronized (this) {
      if (now != lastTimestamp) {
        lastTimestamp = now;
        timesmapStr = simpleFormat.format(new Date(now));
      }
      return timesmapStr;
    }
  }  
}
