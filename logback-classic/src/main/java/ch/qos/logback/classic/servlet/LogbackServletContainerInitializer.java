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

package ch.qos.logback.classic.servlet;

import static ch.qos.logback.core.CoreConstants.DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY;

import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import ch.qos.logback.classic.util.StatusViaSLF4JLoggerFactory;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Attaches a new instance of {@link LogbackServletContextListener} to the
 * current web-applications {@link ServletContext}.
 * 
 * @author Ceki Gulcu
 * @since 1.1.10
 */
public class LogbackServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

        if (isDisabledByConfiguration(ctx)) {
            StatusViaSLF4JLoggerFactory.addInfo("Due to deployment instructions will NOT register an instance of "
                    + LogbackServletContextListener.class + " to the current web-app", this);

            return;
        }

        StatusViaSLF4JLoggerFactory.addInfo(
                "Adding an instance of  " + LogbackServletContextListener.class + " to the current web-app", this);
        LogbackServletContextListener lscl = new LogbackServletContextListener();
        ctx.addListener(lscl);
    }

    /**
     * Search for value of DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY in the web-app
     * first, then as a system property and as an environment variable last.
     *
     * @param ctx
     * @return True if value of DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY is
     *         available and set to "true", false otherwise.
     */
    boolean isDisabledByConfiguration(ServletContext ctx) {
        String disableAttributeStr = null;
        Object disableAttribute = ctx.getInitParameter(DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY);
        if (disableAttribute instanceof String) {
            disableAttributeStr = (String) disableAttribute;
        }

        if (OptionHelper.isNullOrEmptyOrAllSpaces(disableAttributeStr)) {
            disableAttributeStr = OptionHelper.getSystemProperty(DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY);
        }

        if (OptionHelper.isNullOrEmptyOrAllSpaces(disableAttributeStr)) {
            disableAttributeStr = OptionHelper.getEnv(DISABLE_SERVLET_CONTAINER_INITIALIZER_KEY);
        }

        return Boolean.parseBoolean(disableAttributeStr);
    }

}
