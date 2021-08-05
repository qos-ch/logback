/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The default
 * target is <code>System.out</code>.
 * <p>&nbsp;</p>
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#ConsoleAppender
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Tom SH Liu
 * @author Ruediger Dohna
 */

public class ConsoleAppender<E> extends OutputStreamAppender<E> {

    protected ConsoleTarget target = ConsoleTarget.SystemOut;
    protected boolean withJansi = false;

    /**
     * Sets the value of the <b>Target</b> option. Recognized values are
     * "System.out" and "System.err". Any other value will be ignored.
     */
    public void setTarget(String value) {
        ConsoleTarget t = ConsoleTarget.findByName(value.trim());
        if (t == null) {
            targetWarn(value);
        } else {
            target = t;
        }
    }

    /**
     * Returns the current value of the <b>target</b> property. The default value
     * of the option is "System.out".
     * <p>
     * See also {@link #setTarget}.
     */
    public String getTarget() {
        return target.getName();
    }

    private void targetWarn(String val) {
        Status status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ConsoleTarget.values()), this);
        status.add(new WarnStatus("Using previously set target, System.out by default.", this));
        addStatus(status);
    }

    @Override
    public void start() {
        OutputStream targetStream = target.getStream();
        // enable jansi only if withJansi set to true
        if (withJansi) {
            addInfo("Enabling JANSI AnsiPrintStream for the console.");
            OutputStream ansiPrintStream = getAnsiPrintStream();
            if (ansiPrintStream != null) {
                targetStream = ansiPrintStream;
            }
        }
        setOutputStream(targetStream);
        super.start();
    }

    private OutputStream getAnsiPrintStream() {
        try {
            ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
            Class<?> ansiConsoleClass = classLoader.loadClass("org.fusesource.jansi.AnsiConsole");
            Class<?> ansiPrintStreamClass = classLoader.loadClass("org.fusesource.jansi.AnsiPrintStream");
            Method ansiPrintStreamMethod = ansiConsoleClass.getMethod(target == ConsoleTarget.SystemOut ? "out" : "err");
            Method installMethod = ansiPrintStreamClass.getMethod("install");
            OutputStream outputStream = (OutputStream) ansiPrintStreamMethod.invoke(null);
            installMethod.invoke(outputStream);
            return outputStream;
        } catch (Throwable e) {
            addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", e);
        }
        return null;
    }

    /**
     * @return whether to use JANSI or not.
     */
    public boolean isWithJansi() {
        return withJansi;
    }

    /**
     * If true, this appender will output to a stream provided by the JANSI
     * library.
     *
     * @param withJansi whether to use JANSI or not.
     * @since 1.0.5
     */
    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
    }

}
