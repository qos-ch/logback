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
package ch.qos.logback.core.rolling;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.helper.CompressionMode;

/**
 * <code>RollingFileAppender</code> extends {@link FileAppender} to backup the
 * log files depending on {@link RollingPolicy} and {@link TriggeringPolicy}.
 * <p>
 * 
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#RollingFileAppender
 * 
 * @author Heinz Richter
 * @author Ceki G&uuml;lc&uuml;
 */
public class RollingFileAppender<E> extends FileAppender<E> {
  File currentlyActiveFile;
  TriggeringPolicy<E> triggeringPolicy;
  RollingPolicy rollingPolicy;

  /**
   * The default constructor simply calls its {@link FileAppender#FileAppender
   * parents constructor}.
   */
  public RollingFileAppender() {
  }

  public void start() {
    if (triggeringPolicy == null) {
      addWarn("No TriggeringPolicy was set for the RollingFileAppender named "
          + getName());
      addWarn("For more information, please visit http://logback.qos.ch/codes.html#rfa_no_tp");
      return;
    }

    // we don't want to void existing log files
    if (!append) {
      addWarn("Append mode is mandatory for RollingFileAppender");
      append = true;
    }

    if (rollingPolicy == null) {
      addError("No RollingPolicy was set for the RollingFileAppender named "
          + getName());
      addError("For more information, please visit http://logback.qos.ch/codes.html#rfa_no_rp");
      return;
    }

    if (isPrudent()) {
      if (rawFileProperty() != null) {
        addWarn("Setting \"File\" property to null on account of prudent mode");
        setFile(null);
      }
      if (rollingPolicy.getCompressionMode() != CompressionMode.NONE) {
        addError("Compression is not supported in prudent mode. Aborting");
        return;
      }
    }

    currentlyActiveFile = new File(getFile());
    addInfo("Active log file name: " + getFile());
    super.start();
  }

  @Override
  public void setFile(String file) {
    // http://jira.qos.ch/browse/LBCORE-94
    // allow setting the file name to null if mandated by prudent mode
    if (file != null && ((triggeringPolicy != null) || (rollingPolicy != null))) {
      addError("File property must be set before any triggeringPolicy or rollingPolicy properties");
      addError("Visit http://logback.qos.ch/codes.html#rfa_file_after for more information");
    }
    super.setFile(file);
  }

  @Override
  public String getFile() {
    return rollingPolicy.getActiveFileName();
  }

  /**
   * Implemented by delegating most of the rollover work to a rolling policy.
   */
  public synchronized void rollover() {
    // Note: This method needs to be synchronized because it needs exclusive
    // access while it closes and then re-opens the target file.
    //
    // make sure to close the hereto active log file! Renaming under windows
    // does not work for open files.
    this.closeWriter();

    try {
      rollingPolicy.rollover();
    } catch (RolloverFailure rf) {
      addWarn("RolloverFailure occurred. Deferring roll-over.");
      // we failed to roll-over, let us not truncate and risk data loss
      this.append = true;
    }

    try {
      // update the currentlyActiveFile
      // http://jira.qos.ch/browse/LBCORE-90
      currentlyActiveFile = new File(rollingPolicy.getActiveFileName());

      // This will also close the file. This is OK since multiple
      // close operations are safe.
      this.openFile(rollingPolicy.getActiveFileName());
    } catch (IOException e) {
      addError("setFile(" + fileName + ", false) call failed.", e);
    }
  }

  /**
   * This method differentiates RollingFileAppender from its super class.
   */
  @Override
  protected void subAppend(E event) {
    // The roll-over check must precede actual writing. This is the
    // only correct behavior for time driven triggers.

    // We need to synchronize on triggeringPolicy so that only one rollover
    // occurs at a time
    synchronized (triggeringPolicy) {
      if (triggeringPolicy.isTriggeringEvent(currentlyActiveFile, event)) {
        rollover();
      }
    }

    super.subAppend(event);
  }

  public RollingPolicy getRollingPolicy() {
    return rollingPolicy;
  }

  public TriggeringPolicy<E> getTriggeringPolicy() {
    return triggeringPolicy;
  }

  /**
   * Sets the rolling policy. In case the 'policy' argument also implements
   * {@link TriggeringPolicy}, then the triggering policy for this appender is
   * automatically set to be the policy argument.
   * 
   * @param policy
   */
  @SuppressWarnings("unchecked")
  public void setRollingPolicy(RollingPolicy policy) {
    rollingPolicy = policy;
    if (rollingPolicy instanceof TriggeringPolicy) {
      triggeringPolicy = (TriggeringPolicy<E>) policy;
    }

  }

  public void setTriggeringPolicy(TriggeringPolicy<E> policy) {
    triggeringPolicy = policy;
    if (policy instanceof RollingPolicy) {
      rollingPolicy = (RollingPolicy) policy;
    }
  }
}
