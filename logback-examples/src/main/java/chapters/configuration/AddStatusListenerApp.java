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
package chapters.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusManager;

public class AddStatusListenerApp {

    public static void main(final String[] args) throws JoranException {

        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final StatusManager statusManager = lc.getStatusManager();
        final OnConsoleStatusListener onConsoleListener = new OnConsoleStatusListener();
        statusManager.add(onConsoleListener);

        final Logger logger = LoggerFactory.getLogger("myApp");
        logger.info("Entering application.");

        final Foo foo = new Foo();
        foo.doIt();
        logger.info("Exiting application.");
    }
}
