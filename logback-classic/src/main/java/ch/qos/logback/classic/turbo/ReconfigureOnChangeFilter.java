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
import java.util.List;

import ch.qos.logback.classic.gaffer.GafferUtil;
import ch.qos.logback.classic.util.EnvUtil;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.InfoStatus;

/**
 * Reconfigure a LoggerContext when the configuration file changes.
 *
 * @author Ceki Gulcu
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
  URL mainConfigurationURL;
  protected volatile long nextCheck;

  ConfigurationWatchList cwl;

  @Override
  public void start() {
    cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
    if (cwl != null) {
      mainConfigurationURL = cwl.getMainURL();
      List<File> watchList = cwl.getCopyOfFileWatchList();
      long inSeconds = refreshPeriod / 1000;
      addInfo("Will scan for changes in [" + watchList + "] every "
              + inSeconds + " seconds. ");
      synchronized (cwl) {
        updateNextCheck(System.currentTimeMillis());
      }
      super.start();
    }  else {
      addWarn("Empty ConfigurationWatchList in context");
    }
  }

  // The next fields counts the number of time the decide method is called
  //
  // IMPORTANT: This field can be updated by multiple threads. It follows that
  // its values may *not* be incremented sequentially. However, we don't care
  // about the actual value of the field except that from time to time the
  // expression (invocationCounter++ & 0xF) == 0xF) should be true.
  private long invocationCounter = 0;

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

    synchronized (cwl) {
      if (changeDetected()) {
        // Even though reconfiguration involves resetting the loggerContext,
        // which clears the list of turbo filters including this instance, it is
        // still possible for this instance to be subsequently invoked by another
        // thread if it was already executing when the context was reset.
        disableSubsequentReconfiguration();
        detachReconfigurationToNewThread();
      }
    }

    return FilterReply.NEUTRAL;
  }

  // by detaching reconfiguration to a new thread, we release the various
  // locks held by the current thread, in particular, the AppenderAttachable
  // reader lock.
  private void detachReconfigurationToNewThread() {
    addInfo("Detected change in [" + cwl.getCopyOfFileWatchList() + "]");
    addInfo("Resetting and reconfiguring context [" + context.getName() + "]");
    new ReconfiguringThread().start();
  }

  void updateNextCheck(long now) {
    nextCheck = now + refreshPeriod;
  }

  protected boolean changeDetected() {
    long now = System.currentTimeMillis();
    if (now >= nextCheck) {
      updateNextCheck(now);
      return cwl.changeDetected();
    }
    return false;
  }

  void disableSubsequentReconfiguration() {
    nextCheck = Long.MAX_VALUE;
  }

  public long getRefreshPeriod() {
    return refreshPeriod;
  }

  public void setRefreshPeriod(long refreshPeriod) {
    this.refreshPeriod = refreshPeriod;
  }

  class ReconfiguringThread extends Thread {
    public void run() {
      LoggerContext lc = (LoggerContext) context;
      if (mainConfigurationURL.toString().endsWith("groovy")) {
        if (EnvUtil.isGroovyAvailable()) {
          lc.reset();
          // avoid directly referring to GafferConfigurator so as to avoid
          // loading  groovy.lang.GroovyObject . See also http://jira.qos.ch/browse/LBCLASSIC-214
          GafferUtil.runGafferConfiguratorOn(lc, this, mainConfigurationURL);
        } else {
          addError("Groovy classes are not available on the class path. ABORTING INITIALIZATION.");
        }
      } else if (mainConfigurationURL.toString().endsWith("xml")) {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(context);
        lc.reset();
        try {
          jc.doConfigure(mainConfigurationURL);
          lc.getStatusManager().add(
                  new InfoStatus("done resetting the logging context", this));
        } catch (JoranException e) {
          addError("Failure during reconfiguration", e);
        }
      }
    }
  }
}
