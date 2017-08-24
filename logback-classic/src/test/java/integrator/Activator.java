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
package integrator;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A BundleActivator which invokes slf4j loggers
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class Activator implements BundleActivator {

    private BundleContext m_context = null;

    public void start(BundleContext context) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            // the context was probably already configured by default configuration
            // rules
            lc.reset();
            configurator.doConfigure("src/test/input/osgi/simple.xml");
        } catch (JoranException je) {
            je.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Activator.start()");
        m_context = context;
    }

    public void stop(BundleContext context) {
        m_context = null;
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Activator.stop");
    }

    public Bundle[] getBundles() {
        if (m_context != null) {
            return m_context.getBundles();
        }
        return null;
    }
}