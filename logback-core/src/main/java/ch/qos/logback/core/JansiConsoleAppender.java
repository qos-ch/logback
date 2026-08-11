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

import java.io.OutputStream;

import org.jline.jansi.AnsiConsole;

import ch.qos.logback.core.joran.spi.ConsoleTarget;

/**
 * A {@link ConsoleAppender} that always writes through JLine's
 * {@link AnsiConsole}, enabling ANSI sequences on platforms that need Jansi
 * (notably Windows).
 * <p>
 * Unlike {@link ConsoleAppender}'s optional {@code withJansi} path, this
 * class calls {@link AnsiConsole} directly and does not use reflection. It
 * therefore requires {@code org.jline:jansi-core} on the classpath.
 * </p>
 * <p>
 * {@link AnsiConsole#systemInstall()} is paired with
 * {@link AnsiConsole#systemUninstall()} on {@link #stop()} when this appender
 * performed the install. Console streams are still only flushed on stop (not
 * closed); see {@link ConsoleAppender#closeOutputStream()}.
 * </p>
 *
 * @param <E> the type of logging events
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.3
 * @see AnsiConsole
 * @see ConsoleAppender#setWithJansi(boolean)
 */
public class JansiConsoleAppender<E> extends ConsoleAppender<E> {

    /**
     * True after this instance has successfully called
     * {@link AnsiConsole#systemInstall()} and until the matching
     * {@link AnsiConsole#systemUninstall()} on {@link #stop()}.
     */
    private boolean installedByThisAppender;

    /**
     * Always enables the Jansi-backed stream, then delegates to
     * {@link ConsoleAppender#start()}.
     */
    @Override
    public void start() {
        withJansi = true;
        super.start();
    }

    /**
     * Flushes the console stream (via {@link ConsoleAppender#stop()}), then
     * undoes {@link AnsiConsole#systemInstall()} if this appender performed it.
     */
    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            uninstallAnsiConsoleIfInstalledByThisAppender();
        }
    }

    /**
     * Installs Jansi and returns {@link AnsiConsole#out()} or
     * {@link AnsiConsole#err()} according to the configured target.
     * <p>
     * No reflection is used. {@link AnsiConsole#systemInstall()} is invoked at
     * most once per successful install ownership of this instance.
     * </p>
     */
    @Override
    protected OutputStream wrapWithJansi(OutputStream targetStream) {
        try {
            addInfo("Enabling JANSI AnsiPrintStream via org.jline.jansi.AnsiConsole.");
            if (!installedByThisAppender) {
                AnsiConsole.systemInstall();
                installedByThisAppender = true;
            }
            if (target == ConsoleTarget.SystemErr) {
                return AnsiConsole.err();
            } else {
                return AnsiConsole.out();
            }
        } catch (Exception e) {
            addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", e);
            return targetStream;
        }
    }

    private void uninstallAnsiConsoleIfInstalledByThisAppender() {
        if (!installedByThisAppender) {
            return;
        }
        installedByThisAppender = false;
        try {
            AnsiConsole.systemUninstall();
            addInfo("Uninstalled JANSI AnsiConsole previously installed by this appender.");
        } catch (RuntimeException e) {
            addWarn("Failed to uninstall AnsiConsole.", e);
        }
    }

}
