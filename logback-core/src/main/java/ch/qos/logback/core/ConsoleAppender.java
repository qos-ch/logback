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
package ch.qos.logback.core;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;


/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The default
 * target is <code>System.out</code>.
 *
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#ConsoleAppender
 *
 * @author Ceki G&uuml;lc&uuml;
 */

public class ConsoleAppender<E> extends WriterAppender<E> {

    public static final String SYSTEM_OUT = "System.out";
    public static final String SYSTEM_ERR = "System.err";
    protected String target = SYSTEM_OUT;

    /**
     * As in most logback components, the default constructor does nothing.
     */
    public ConsoleAppender() {
    }

    /**
     * Sets the value of the <b>Target</b> option. Recognized values are
     * "System.out" and "System.err". Any other value will be ignored.
     */
    public void setTarget(String value) {
      String v = value.trim();

      if (SYSTEM_OUT.equalsIgnoreCase(v)) {
        target = SYSTEM_OUT;
      } else if (SYSTEM_ERR.equalsIgnoreCase(v)) {
        target = SYSTEM_ERR;
      } else {
        targetWarn(value);
      }
    }

    /**
     * Returns the current value of the <b>Target</b> property. The default
     * value of the option is "System.out".
     * 
     * See also {@link #setTarget}.
     */
    public String getTarget() {
      return target;
    }

    void targetWarn(String val) {
      Status status = new WarnStatus("["+val+" should be System.out or System.err.", this);
      status.add(new WarnStatus("Using previously set target, System.out by default.", this));
      addStatus(status);
    }

    public void start() {
      if (target.equals(SYSTEM_OUT)) {
        setWriter(createWriter(System.out));
      } else {
        setWriter(createWriter(System.err));
      }
      super.start();
    }

    /**
     * This method overrides the parent {@link WriterAppender#closeWriter}
     * implementation  because the console stream is not ours to close.
     */
    protected final void closeWriter() {
      writeFooter();
    }
  }


