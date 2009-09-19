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
 */
public class ReconfigureOnChangeFilter extends TurboFilter {

  final static long INIT = System.currentTimeMillis();
  final static long SENTINEL = Long.MAX_VALUE;

  /**
   * Scan for changes in configuration file once every minute.
   */
  // 1 minute - value mentioned in documentation
  public final static long DEFAULT_REFRESH_PERIOD = 60 * 1000;

  long refreshPeriod = DEFAULT_REFRESH_PERIOD;
  File fileToScan;
  protected long nextCheck;
  long lastModified;

  @Override
  public void start() {
    URL url = (URL) context
        .getObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN);
    if (url != null) {
      fileToScan = convertToFile(url);
      if (fileToScan != null) {
        synchronized (context) {
          long inSeconds = refreshPeriod / 1000;
          addInfo("Will scan for changes in file [" + fileToScan + "] every "
              + inSeconds + " seconds. ");
          lastModified = fileToScan.lastModified();
          updateNextCheck(System.currentTimeMillis());
        }
        super.start();
      }
    } else {
      addError("Could not find URL of file to scan.");
    }
  }

  File convertToFile(URL url) {
    String protocol = url.getProtocol();
    if ("file".equals(protocol)) {
      File file = new File(url.getFile());
      return file;
    } else {
      addError("URL [" + url + "] is not of type file");
      return null;
    }
  }

  // The next fields counts the number of time the decide method is called
  //
  // IMPORTANT: This field can be updated by multiple threads. It follows that
  // its values may *not* be incremented sequentially. However, we don't care
  // about the actual value of the field except that from time to time the
  // expression (invocationCounter++ & 0xF) == 0xF) should be true.
  private int invocationCounter = 0;

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level,
      String format, Object[] params, Throwable t) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }

    // for performance reasons, check for changes every 16 invocations
    if (((invocationCounter++) & 0xF) != 0xF) {
      return FilterReply.NEUTRAL;
    }

    synchronized (context) {
      boolean changed = changeDetected();
      if (changed) {
        addInfo("Detected change in [" + fileToScan + "]");
        addInfo("Resetting and reconfiguring context [" + context.getName()
            + "]");
        reconfigure();
      }
    }
    return FilterReply.NEUTRAL;
  }

  void updateNextCheck(long now) {
    nextCheck = now + refreshPeriod;
  }

//  String stem() {
//    return currentThreadName() + ", context " + context.getName()
//        + ", nextCheck=" + (nextCheck - INIT);
//  }

  // This method is synchronized to prevent near-simultaneous re-configurations
  protected boolean changeDetected() {
    long now = System.currentTimeMillis();
    if (now >= nextCheck) {
      updateNextCheck(now);
      return (lastModified != fileToScan.lastModified() && lastModified != SENTINEL);
    }
    return false;
  }

  String currentThreadName() {
    return Thread.currentThread().getName();
  }

  void disableSubsequentRecofiguration() {
    lastModified = SENTINEL;
  }

  protected void reconfigure() {
    // Even though this method resets the loggerContext, which clears the list
    // of turbo filters including this instance, it is still possible for this
    // instance to be subsequently invoked by another thread if it was already
    // executing when the context was reset.
    // We prevent multiple reconfigurations (for the same file change event) by
    // setting an appropriate sentinel value for lastMofidied field.
    disableSubsequentRecofiguration();
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
