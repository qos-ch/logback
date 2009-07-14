/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2009, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.classic.turbo;

import java.io.File;
import java.net.URL;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Reconfigure a LoggerContext when the configuration file changes.
 * 
 * @author Ceki Gulcu 
 *  
 *  */
public class ReconfigureOnChangeFilter extends TurboFilter {

  final static long DEFAULT_REFRESH_PERIOD = 60*1000; // 1 minute
  long refreshPeriod = DEFAULT_REFRESH_PERIOD;
  File fileToScan;
  protected long nextCheck;
  long lastModified;


  @Override
  public void start() {
    URL url = (URL) context.getObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN);
    if(url != null) {
      fileToScan = convertToFile(url);
      if(fileToScan != null) {
        long inSeconds = refreshPeriod/1000;
        addInfo("Will scan for changes in file ["+fileToScan+"] every "+inSeconds+" seconds");
        lastModified = fileToScan.lastModified();
        updateNextCheck(System.currentTimeMillis());
        super.start();
      }
    } else {
     addError("Could not find URL of file to scan.");
    }
  }
  
  File convertToFile(URL url) {
    String protocol = url.getProtocol();
    if("file".equals(protocol)) {
      File file = new File(url.getFile());
      return file;
    } else {
      addError("URL ["+url+"] is not of type file");
      return null;
    }
  }
  
  // a counter of the number of time the decide method is called
  private volatile int invocationCounter =  0;
  
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    if(!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    System.out.println("counter="+invocationCounter+", format="+format);
    // for performance reasons, check for changes every 16 invocations
    if(((invocationCounter++) & 0xF) != 0xF) {
      return FilterReply.NEUTRAL;
    }

    
    boolean changed = changeDetected();
    if(changed) {
      addInfo("["+fileToScan + "] change detected. Reconfiguring");
      addInfo("Resetting and reconfiguring context ["+context.getName()+"]");
      reconfigure();
    }
    return FilterReply.NEUTRAL;
  }

  void updateNextCheck(long now) {
    nextCheck = now + refreshPeriod;
  }
 
  protected boolean changeDetected() {
    long now = System.currentTimeMillis();
    if(now >= nextCheck) {
      updateNextCheck(now);
      return (lastModified != fileToScan.lastModified());
    }
    return false;
  }
  
  
  protected void reconfigure() {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    LoggerContext lc = (LoggerContext) context;
    lc.reset();
    try {
      jc.doConfigure(fileToScan);
    } catch (JoranException e) {
      addError("Failure during reconfiguration", e);
    }  
  }

  public long getRefreshPeriod() {
    return refreshPeriod;
  }

  public void setRefreshPeriod(long refreshPeriod) {
    this.refreshPeriod = refreshPeriod;
  }
}
