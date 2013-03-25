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
package ch.qos.logback.classic.net.server;

import java.io.PrintStream;

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusUtil;

/**
 * A simple {@link Context} that records status events (typically during
 * socket server startup only) to the console.
 *
 * @author Carl Harris
 */
public class ServerContext extends ContextBase {

  private final ConsoleStatusListener listener;
  
  public ServerContext() {
    this(System.out, System.err);
  }
  
  public ServerContext(PrintStream out, PrintStream err) {
    this.listener = new ConsoleStatusListener(out, err);
    super.getStatusManager().add(listener);
  }
  
  /**
   * Adds an error to the receiver.
   * @param message error message
   */
  public void addError(String message) {
    StatusUtil.addError(this, this, message, null);
  }

  /**
   * Adds an error to the receiver.
   * @param message error message
   * @param t throwable cause of the error
   */
  public void addError(String message, Throwable t) {
    StatusUtil.addError(this, this, message, t);
  }
  
  /**
   * Gets a flag indicating whether one or more errors have been recorded
   * by the receiver
   * @return {@code true} if one or more errors have been recorded
   */
  public boolean hasErrors() {
    return listener.hasErrors();
  }
  
  /**
   * A {@link StatusListener} that records messages to the console.
   */
  static class ConsoleStatusListener implements StatusListener {

    private final PrintStream out;
    private final PrintStream err;
    
    private int errorCount;
   
    public ConsoleStatusListener(PrintStream out, PrintStream err) {
      this.out = out;
      this.err = err;
    }
    
    public void addStatusEvent(Status status) {
      PrintStream out = this.out;
      if (status.getEffectiveLevel() == Status.ERROR) {
        out = this.err;
        errorCount++;
      }
      if (status.getEffectiveLevel() == Status.INFO) {
        return;
      }
      out.println(status.getMessage());
      if (status.getThrowable() != null) {
        status.getThrowable().printStackTrace(out);
      }
    }

    public boolean hasErrors() {
      return errorCount > 0;
    }
    
  }

}
