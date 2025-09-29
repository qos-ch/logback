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

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The default
 * target is <code>System.out</code>.
 * <p>
 * &nbsp;
 * </p>
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

    private final static String AnsiConsole_CLASS_NAME = "org.fusesource.jansi.AnsiConsole";
    private final static String JANSI2_OUT_METHOD_NAME = "out";
    private final static String JANSI2_ERR_METHOD_NAME = "err";
    private final static String WRAP_SYSTEM_OUT_METHOD_NAME = "wrapSystemOut";
    private final static String WRAP_SYSTEM_ERR_METHOD_NAME = "wrapSystemErr";
    private final static String SYSTEM_INSTALL_METHOD_NAME = "systemInstall";
    private final static Class<?>[] ARGUMENT_TYPES = { PrintStream.class };

    private final static String CONSOLE_APPENDER_WARNING_URL = CoreConstants.CODES_URL+"#slowConsole";

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
     * Returns the current value of the <b>target</b> property. The default value of
     * the option is "System.out".
     * <p>
     * See also {@link #setTarget}.
     */
    public String getTarget() {
        return target.getName();
    }

    private void targetWarn(String val) {
        Status status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ConsoleTarget.values()),
                this);
        status.add(new WarnStatus("Using previously set target, System.out by default.", this));
        addStatus(status);
    }

    @Override
    public void start() {
        addInfo("NOTE: Writing to the console can be slow. Try to avoid logging to the ");
        addInfo("console in production environments, especially in high volume systems.");
        addInfo("See also "+CONSOLE_APPENDER_WARNING_URL);
        OutputStream targetStream = target.getStream();
        // enable jansi only if withJansi set to true
        if (withJansi) {
            targetStream = wrapWithJansi(targetStream);
        }
        setOutputStream(targetStream);
        super.start();
    }

    private OutputStream wrapWithJansi(OutputStream targetStream) {
        try {
            addInfo("Enabling JANSI AnsiPrintStream for the console.");
            ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
            Class<?> classObj = classLoader.loadClass(AnsiConsole_CLASS_NAME);

            Method systemInstallMethod  = classObj.getMethod(SYSTEM_INSTALL_METHOD_NAME);
            if(systemInstallMethod != null) {
                systemInstallMethod.invoke(null);
            }

//            final Optional<Method> optSystemInstallMethod = Arrays.stream(classObj.getMethods())
//                            .filter(m -> m.getName().equals(SYSTEM_INSTALL_METHOD_NAME))
//                            .filter(m -> m.getParameters().length == 0)
//                            .filter(m -> Modifier.isStatic(m.getModifiers()))
//                            .findAny();
//
//            if (optSystemInstallMethod.isPresent()) {
//                final Method systemInstallMethod = optSystemInstallMethod.orElseThrow(() -> new NoSuchElementException("No systemInstall method present"));
//                systemInstallMethod.invoke(null);
//            }

            // check for JAnsi 2
            String methodNameJansi2 = target == ConsoleTarget.SystemOut ? JANSI2_OUT_METHOD_NAME
                    : JANSI2_ERR_METHOD_NAME;
            final Optional<Method> optOutMethod = Arrays.stream(classObj.getMethods())
                    .filter(m -> m.getName().equals(methodNameJansi2))
                    .filter(m -> m.getParameters().length == 0)
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> PrintStream.class.isAssignableFrom(m.getReturnType()))
                    .findAny();
            if (optOutMethod.isPresent()) {
                final Method outMethod = optOutMethod.orElseThrow(() -> new NoSuchElementException("No out/err method present"));
                return (PrintStream) outMethod.invoke(null);
            }

            // JAnsi 1
            String methodName = target == ConsoleTarget.SystemOut ? WRAP_SYSTEM_OUT_METHOD_NAME
                    : WRAP_SYSTEM_ERR_METHOD_NAME;
            Method method = classObj.getMethod(methodName, ARGUMENT_TYPES);
            return (OutputStream) method.invoke(null, new PrintStream(targetStream));
        } catch (Exception e) {
            addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", e);
        }
        return targetStream;
    }

    /**
     * @return whether to use JANSI or not.
     */
    public boolean isWithJansi() {
        return withJansi;
    }

    /**
     * If true, this appender will output to a stream provided by the JANSI library.
     *
     * @param withJansi whether to use JANSI or not.
     * @since 1.0.5
     */
    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
    }

}
