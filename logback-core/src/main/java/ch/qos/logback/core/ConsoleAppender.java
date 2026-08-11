/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.ReentryGuard;
import ch.qos.logback.core.util.ReentryGuardFactory;

/**
 * ConsoleAppender appends log events to <code>System.out</code> or
 * <code>System.err</code> using a layout specified by the user. The default
 * target is <code>System.out</code>.
 * <p>
 * &nbsp;
 * </p>
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#ConsoleAppender
 * <p>
 * This appender does not own the console streams it writes to. On stop it
 * flushes those streams but does not close them, so process-wide stdout/stderr
 * (and Jansi streams wrapping them) remain usable after reconfiguration.
 * </p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Tom SH Liu
 * @author Ruediger Dohna
 * @see <a href="https://github.com/qos-ch/logback/issues/1063">logback issue #1063</a>
 */

public class ConsoleAppender<E> extends OutputStreamAppender<E> {

    protected ConsoleTarget target = ConsoleTarget.SystemOut;
    protected boolean withJansi = false;


    public final static String JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME = "org.jline.jansi.AnsiConsole";
    public final static String FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME = "org.fusesource.jansi.AnsiConsole";

    // Jansi was migrated from FuseSource (org.fusesource.jansi) to JLine (org.jline.jansi), which
    // changed the package of AnsiConsole. Probe the JLine coordinates first, then fall back to the
    // legacy FuseSource ones so that <withJansi> keeps working with both artifacts. See LOGBACK issue 1043.
    private final static String[] ANSI_CONSOLE_CLASS_NAMES = { JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME,
            FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME };

    protected String preferredJansiClassName = null;

    private final static String JANSI2_OUT_METHOD_NAME = "out";
    private final static String JANSI2_ERR_METHOD_NAME = "err";
    private final static String WRAP_SYSTEM_OUT_METHOD_NAME = "wrapSystemOut";
    private final static String WRAP_SYSTEM_ERR_METHOD_NAME = "wrapSystemErr";
    private final static String SYSTEM_INSTALL_METHOD_NAME = "systemInstall";
    private final static Class<?>[] ARGUMENT_TYPES = { PrintStream.class };

    private final static String CONSOLE_APPENDER_WARNING_URL = CoreConstants.CODES_URL+"#slowConsole";

    /**
     * Deprecation notice for {@link #setWithJansi(boolean)} / {@code <withJansi>}.
     * {@link JansiConsoleAppender} is the recommended replacement.
     */
    static final String WITH_JANSI_DEPRECATED_MSG =
            "ConsoleAppender.withJansi is deprecated and will be removed in a future release. "
                    + "Use ch.qos.logback.core.JansiConsoleAppender instead.";

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

    /**
     *
     * @return the preferred Jansi class name
     */
    public String getPreferredJansiClassName() {
        return preferredJansiClassName;
    }

    /**
     * It allows to force Jansi class name used for probing.
     *
     * <p>Used for testing purposes.</p>
     * <p>Valid values are {@link #JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME} and
     * {@link #FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME}.</p>
     *
     * @param preferredJansiClassName the preferred Jansi class name
     * @since 1.6.1
     */
    public void setPreferredJansiClassName(String preferredJansiClassName) {
        this.preferredJansiClassName = preferredJansiClassName;
    }

    private boolean isValidPreferredJansiClassName(String className) {
        return JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME.equals(className)
                || FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME.equals(className);
    }

    private void preferredJansiClassNameWarn(String val) {
        Status status = new WarnStatus(
                "[" + val + "] should be one of " + Arrays.toString(ANSI_CONSOLE_CLASS_NAMES), this);
        status.add(new WarnStatus("Ignoring preferredJansiClassName, using default probing order.", this));
        addStatus(status);
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
        OutputStream targetStream = wrapTarget(target.getStream());
        setOutputStream(targetStream);
        super.start();
    }

    /**
     * Flush the console target without closing it.
     * <p>
     * {@link System#out}, {@link System#err}, and Jansi streams built on
     * {@code FileDescriptor.out}/{@code err} are process-wide resources. Closing
     * them from {@link OutputStreamAppender#stop()} would poison stdout for the
     * rest of the JVM (see
     * <a href="https://github.com/qos-ch/logback/issues/1063">issue #1063</a>).
     * The non-Jansi {@link ConsoleTarget} streams already no-op on
     * {@code close()}; this override keeps the same contract when Jansi is used.
     * </p>
     */
    @Override
    protected void closeOutputStream() {
        if (getOutputStream() != null) {
            try {
                // write encoder footer while the stream is still attached
                encoderClose();
                getOutputStream().flush();
            } catch (IOException e) {
                addStatus(new ErrorStatus("Could not flush output stream for ConsoleAppender.", this, e));
            }
        }
    }

    /**
     * Create a ThreadLocal ReentryGuard to prevent recursive appender invocations.
     * @return a ReentryGuard instance of type {@link ReentryGuardFactory.GuardType#THREAD_LOCAL THREAD_LOCAL}.
     */
    protected ReentryGuard buildReentryGuard() {
        return ReentryGuardFactory.makeGuard(ReentryGuardFactory.GuardType.THREAD_LOCAL);
    }

    /**
     * Optionally wraps the raw console target stream before it is used for
     * logging.
     * <p>
     * The default implementation applies the deprecated {@code withJansi}
     * reflection path when {@link #withJansi} is true. Subclasses such as
     * {@link JansiConsoleAppender} should override this method to supply an
     * alternate stream (without relying on {@code withJansi}).
     * </p>
     *
     * @param targetStream the raw stream for {@link #target}
     * @return the stream to use as this appender's output stream
     * @since 1.6.3
     */
    protected OutputStream wrapTarget(OutputStream targetStream) {
        if (withJansi) {
            return wrapWithJansi(targetStream);
        } else {
            return targetStream;
        }
    }

    /**
     * Wraps the console target stream with a Jansi ANSI-aware stream using
     * reflection.
     * <p>
     * This path is <strong>deprecated</strong>. Prefer {@link JansiConsoleAppender},
     * which overrides {@link #wrapTarget(OutputStream)} and calls
     * {@code org.jline.jansi.AnsiConsole} directly.
     * </p>
     *
     * @param targetStream the raw console target stream
     * @return a Jansi-backed stream, or {@code targetStream} on failure
     * @deprecated Use {@link JansiConsoleAppender} / override {@link #wrapTarget(OutputStream)}.
     */
    @Deprecated
    protected OutputStream wrapWithJansi(OutputStream targetStream) {
        addWarn(WITH_JANSI_DEPRECATED_MSG);
        try {
            addInfo("Enabling JANSI AnsiPrintStream for the console.");
            ClassLoader classLoader = Loader.getClassLoaderOfObject(context);
            Class<?> classObj = loadAnsiConsoleClass(classLoader);

            Method systemInstallMethod  = classObj.getMethod(SYSTEM_INSTALL_METHOD_NAME);
            if(systemInstallMethod != null) {
                systemInstallMethod.invoke(null);
            }

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
     * Loads the Jansi {@code AnsiConsole} class.
     * <p>
     * If {@link #preferredJansiClassName} is set to a valid value
     * ({@link #JLINE_JANSI_ANSI_CONSOLE_CLASS_NAME} or {@link #FUSESOURCE_JANSI_ANSI_CONSOLE_CLASS_NAME}),
     * that class is loaded. An invalid preferred value is reported and ignored.
     * <p>
     * If {@code preferredJansiClassName} is not set (or was invalid), candidates are probed in
     * {@link #ANSI_CONSOLE_CLASS_NAMES} order (JLine's {@code org.jline.jansi} first, then the legacy
     * FuseSource {@code org.fusesource.jansi}). This keeps {@code <withJansi>} working across the Jansi
     * migration from FuseSource to JLine.
     *
     * @throws ClassNotFoundException if none of the candidate classes is available.
     */
    Class<?> loadAnsiConsoleClass(ClassLoader classLoader) throws ClassNotFoundException {
        if (preferredJansiClassName != null) {
            if (isValidPreferredJansiClassName(preferredJansiClassName)) {
                return classLoader.loadClass(preferredJansiClassName);
            } else {
                preferredJansiClassNameWarn(preferredJansiClassName);
            }
        }
        ClassNotFoundException lastException = null;
        for (String className : ANSI_CONSOLE_CLASS_NAMES) {
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                lastException = e;
            }
        }
        throw lastException;
    }

    /**
     * @return whether to use JANSI or not.
     * @deprecated Use {@link JansiConsoleAppender} instead of {@code withJansi} on
     *             {@link ConsoleAppender}.
     */
    @Deprecated
    public boolean isWithJansi() {
        return withJansi;
    }

    /**
     * If true, this appender will output to a stream provided by the JANSI library
     * via reflection.
     * <p>
     * This option is <strong>deprecated</strong>. Use {@link JansiConsoleAppender},
     * which talks to {@code org.jline.jansi.AnsiConsole} directly, instead of
     * {@code <withJansi>true</withJansi>} on a plain {@link ConsoleAppender}.
     * </p>
     *
     * @param withJansi whether to use JANSI or not.
     * @since 1.0.5
     * @deprecated Use {@link JansiConsoleAppender} instead.
     */
    @Deprecated
    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
        if (withJansi) {
            addWarn(WITH_JANSI_DEPRECATED_MSG);
        }
    }

}
