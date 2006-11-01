/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.rolling;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.core.FileAppender;


/**
 * <code>RollingFileAppender</code> extends {@link FileAppender} to backup the log files
 * depending on {@link RollingPolicy} and {@link TriggeringPolicy}.
 * <p>
 * To be of any use, a <code>RollingFileAppender</code> instance must have both 
 * a <code>RollingPolicy</code> and a <code>TriggeringPolicy</code> set up. 
 * However, if its <code>RollingPolicy</code> also implements the
 * <code>TriggeringPolicy</code> interface, then only the former needs to be
 * set up. For example, {@link TimeBasedRollingPolicy} acts both as a
 * <code>RollingPolicy</code> and a <code>TriggeringPolicy</code>.
 * 
 * <p><code>RollingFileAppender</code> can be configured programattically or
 * using {@link ch.qos.logback.classic.joran.JoranConfigurator}. Here is a sample
 * configration file:

<pre>&lt;?xml version="1.0" encoding="UTF-8" ?>

&lt;configuration debug="true">

  &lt;appender name="ROLL" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <b>&lt;rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      &lt;param name="FileNamePattern" value="/wombat/foo.%d{yyyy-MM}.gz"/>
    &lt;/rollingPolicy></b>

    &lt;layout class="ch.qos.logback.classic.PatternLayout">
      &lt;param name="Pattern" value="%c{1} - %m%n"/>
    &lt;/layout>     
  &lt;/appender>

  &lt;root">
    &lt;appender-ref ref="ROLL"/>
  &lt;/root>
 
&lt;/configuration>
</pre>

 *<p>This configuration file specifies a monthly rollover schedule including
 * automatic compression of the archived files. See 
 * {@link TimeBasedRollingPolicy} for more details.
 * 
 * @author Heinz Richter
 * @author Ceki G&uuml;lc&uuml;
 * @since  1.3
 * */
public class RollingFileAppender extends FileAppender {
  File activeFileCache;
  TriggeringPolicy triggeringPolicy;
  RollingPolicy rollingPolicy;

  /**
   * The default constructor simply calls its {@link
   * FileAppender#FileAppender parents constructor}.
   * */
  public RollingFileAppender() {
  }

  public void start() {
    if (triggeringPolicy == null) {
      addWarn("No TriggeringPolicy was set for the RollingFileAppender named "+ getName());
      addWarn("For more information, please visit http://logback.qos.ch/codes.html#rfa_no_tp");
      return;
    }

    if (rollingPolicy != null) {  
      //if no active file name was set, then it's the responsability of the
      //rollingPolicy to create one.
      if (getFile() == null) {
        setFile(rollingPolicy.getActiveFileName());
      }
      
      activeFileCache = new File(getFile());
      addInfo("Active log file name: "+ getFile());
      
      super.start();
    } else {
      addWarn("No RollingPolicy was set for the RollingFileAppender named "+ getName());
      addWarn("For more information, please visit http://logback.qos.ch/codes.html#rfa_no_rp");
    }
  }

  /**
     Implements the usual roll over behaviour.

     <p>If <code>MaxBackupIndex</code> is positive, then files
     {<code>File.1</code>, ..., <code>File.MaxBackupIndex -1</code>}
     are renamed to {<code>File.2</code>, ...,
     <code>File.MaxBackupIndex</code>}. Moreover, <code>File</code> is
     renamed <code>File.1</code> and closed. A new <code>File</code> is
     created to receive further log output.

     <p>If <code>MaxBackupIndex</code> is equal to zero, then the
     <code>File</code> is truncated with no backup files created.

   */
  public void rollover() {
    // Note: synchronization at this point is unnecessary as the doAppend 
    // is already synched
    
    //
    // make sure to close the hereto active log file! Renaming under windows
    // does not work for open files.
    this.closeWriter();    
    
    // By default, the newly created file will be created in truncate mode.
    // (See the setFile() call a few lines below.)
    this.append = false;
    try { 
      rollingPolicy.rollover();
    } catch(RolloverFailure rf) {
      addWarn("RolloverFailure occurred. Deferring roll-over.");
      // we failed to roll-over, let us not truncate and risk data loss
      this.append = true;
    }

    try {
      // This will also close the file. This is OK since multiple
      // close operations are safe.
      this.setFile();
    } catch (IOException e) {
      addError(
        "setFile(" + fileName + ", false) call failed.", e);
    }
  }

  /**
     This method differentiates RollingFileAppender from its super
     class.
  */
  protected void subAppend(Object event) {
    // The roll-over check must precede actual writing. This is the 
    // only correct behavior for time driven triggers. 
    if (triggeringPolicy.isTriggeringEvent(activeFileCache, event)) {
      rollover();
    }
      
    super.subAppend(event);
  }

  public RollingPolicy getRollingPolicy() {
    return rollingPolicy;
  }

  public TriggeringPolicy getTriggeringPolicy() {
    return triggeringPolicy;
  }

  /**
   * Sets the rolling policy. In case the 'policy' argument also implements
   * {@link TriggeringPolicy}, then the triggering policy for this appender
   * is automatically set to be the policy argument.
   * @param policy
   */
  public void setRollingPolicy(RollingPolicy policy) {
    rollingPolicy = policy;
    if(rollingPolicy instanceof TriggeringPolicy) {
      triggeringPolicy = (TriggeringPolicy) policy;
    }
    
  }

  public void setTriggeringPolicy(TriggeringPolicy policy) {
    triggeringPolicy = policy;
    if(policy instanceof RollingPolicy) {
      rollingPolicy = (RollingPolicy) policy;
    }
  }
}
