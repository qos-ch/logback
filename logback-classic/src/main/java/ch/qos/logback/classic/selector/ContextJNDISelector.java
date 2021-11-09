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
package ch.qos.logback.classic.selector;

import static ch.qos.logback.classic.ClassicConstants.JNDI_CONFIGURATION_RESOURCE;
import static ch.qos.logback.classic.ClassicConstants.JNDI_CONTEXT_NAME;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.classic.util.JNDIUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A class that allows the LoggerFactory to access an environment-based
 * LoggerContext.
 * <p/>
 * To add in catalina.sh
 * <p/>
 * JAVA_OPTS="$JAVA_OPTS "-Dlogback.ContextSelector=JNDI""
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class ContextJNDISelector implements ContextSelector {

    private final Map<String, LoggerContext> synchronizedContextMap;
    private final LoggerContext defaultContext;

    private static final ThreadLocal<LoggerContext> threadLocal = new ThreadLocal<>();

    public ContextJNDISelector(final LoggerContext context) {
        synchronizedContextMap = Collections.synchronizedMap(new HashMap<String, LoggerContext>());
        defaultContext = context;
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultContext;
    }

    @Override
    public LoggerContext detachLoggerContext(final String loggerContextName) {
        return synchronizedContextMap.remove(loggerContextName);
    }

    @Override
    public LoggerContext getLoggerContext() {
        String contextName = null;
        Context ctx = null;

        // First check if ThreadLocal has been set already
        final LoggerContext lc = threadLocal.get();
        if (lc != null) {
            return lc;
        }

        try {
            // We first try to find the name of our
            // environment's LoggerContext
            ctx = JNDIUtil.getInitialContext();
            contextName = JNDIUtil.lookup(ctx, JNDI_CONTEXT_NAME);
        } catch (final NamingException ne) {
            // We can't log here
        }

        if (contextName == null) {
            // We return the default context
            return defaultContext;
        }
        // Let's see if we already know such a context
        LoggerContext loggerContext = synchronizedContextMap.get(contextName);

        if (loggerContext == null) {
            // We have to create a new LoggerContext
            loggerContext = new LoggerContext();
            loggerContext.setName(contextName);
            synchronizedContextMap.put(contextName, loggerContext);
            final URL url = findConfigFileURL(ctx, loggerContext);
            if (url != null) {
                configureLoggerContextByURL(loggerContext, url);
            } else {
                try {
                    new ContextInitializer(loggerContext).autoConfig();
                } catch (final JoranException je) {
                }
            }
            // logback-292
            if (!StatusUtil.contextHasStatusListener(loggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
            }
        }
        return loggerContext;
    }

    private String conventionalConfigFileName(final String contextName) {
        return "logback-" + contextName + ".xml";
    }

    private URL findConfigFileURL(final Context ctx, final LoggerContext loggerContext) {
        final StatusManager sm = loggerContext.getStatusManager();

        final String jndiEntryForConfigResource = JNDIUtil.lookup(ctx, JNDI_CONFIGURATION_RESOURCE);
        // Do we have a dedicated configuration file?
        if (jndiEntryForConfigResource == null) {
            final String resourceByConvention = conventionalConfigFileName(loggerContext.getName());
            return urlByResourceName(sm, resourceByConvention);
        }
        sm.add(new InfoStatus("Searching for [" + jndiEntryForConfigResource + "]", this));
        final URL url = urlByResourceName(sm, jndiEntryForConfigResource);
        if (url == null) {
            final String msg = "The jndi resource [" + jndiEntryForConfigResource + "] for context [" + loggerContext.getName()
            + "] does not lead to a valid file";
            sm.add(new WarnStatus(msg, this));
        }
        return url;
    }

    private URL urlByResourceName(final StatusManager sm, final String resourceName) {
        sm.add(new InfoStatus("Searching for [" + resourceName + "]", this));
        final URL url = Loader.getResource(resourceName, Loader.getTCL());
        if (url != null) {
            return url;
        }
        return Loader.getResourceBySelfClassLoader(resourceName);
    }

    private void configureLoggerContextByURL(final LoggerContext context, final URL url) {
        try {
            final JoranConfigurator configurator = new JoranConfigurator();
            context.reset();
            configurator.setContext(context);
            configurator.doConfigure(url);
        } catch (final JoranException e) {
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    @Override
    public List<String> getContextNames() {
        return new ArrayList<>(synchronizedContextMap.keySet());
    }

    @Override
    public LoggerContext getLoggerContext(final String name) {
        return synchronizedContextMap.get(name);
    }

    /**
     * Returns the number of managed contexts Used for testing purposes
     *
     * @return the number of managed contexts
     */
    public int getCount() {
        return synchronizedContextMap.size();
    }

    /**
     * These methods are used by the LoggerContextFilter.
     * <p/>
     * They provide a way to tell the selector which context to use, thus saving
     * the cost of a JNDI call at each new request.
     *
     * @param context
     */
    public void setLocalContext(final LoggerContext context) {
        threadLocal.set(context);
    }

    public void removeLocalContext() {
        threadLocal.remove();
    }

}
