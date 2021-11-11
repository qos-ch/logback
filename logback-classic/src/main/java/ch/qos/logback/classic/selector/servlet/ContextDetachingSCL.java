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
package ch.qos.logback.classic.selector.servlet;

import static ch.qos.logback.classic.ClassicConstants.JNDI_CONTEXT_NAME;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.classic.util.JNDIUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ContextDetachingSCL implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        // do nothing
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        String loggerContextName = null;

        try {
            final Context ctx = JNDIUtil.getInitialContext();
            loggerContextName = JNDIUtil.lookup(ctx, JNDI_CONTEXT_NAME);
        } catch (final NamingException ne) {
        }

        if (loggerContextName != null) {
            System.out.println("About to detach context named " + loggerContextName);

            final ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
            if (selector == null) {
                System.out.println("Selector is null, cannot detach context. Skipping.");
                return;
            }
            final LoggerContext context = selector.getLoggerContext(loggerContextName);
            if (context != null) {
                final Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
                logger.warn("Stopping logger context " + loggerContextName);
                selector.detachLoggerContext(loggerContextName);
                // when the web-app is destroyed, its logger context should be stopped
                context.stop();
            } else {
                System.out.println("No context named " + loggerContextName + " was found.");
            }
        }
    }

}
