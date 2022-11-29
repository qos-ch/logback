/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.util;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

// contributors
// Ted Graham, Matt Fowles, see also http://jira.qos.ch/browse/LBCORE-32

/**
 * This class contains logback's logic for automatic configuration
 *
 * @author Ceki Gulcu
 */
public class ContextInitializer {

    final public static String AUTOCONFIG_FILE = DefaultJoranConfigurator.AUTOCONFIG_FILE;
    final public static String TEST_AUTOCONFIG_FILE = DefaultJoranConfigurator.TEST_AUTOCONFIG_FILE;
    /**
     * @deprecated Please use ClassicConstants.CONFIG_FILE_PROPERTY instead
     */
    final public static String CONFIG_FILE_PROPERTY = ClassicConstants.CONFIG_FILE_PROPERTY;

    final LoggerContext loggerContext;

    public ContextInitializer(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    /**
     * This method is deprecated with no replacement
     * @param url
     * @throws JoranException
     */
    @Deprecated
    public void configureByResource(URL url) throws JoranException {
        if (url == null) {
            throw new IllegalArgumentException("URL argument cannot be null");
        }
        final String urlString = url.toString();
        if (urlString.endsWith("xml")) {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            configurator.doConfigure(url);
        } else {
            throw new LogbackException(
                    "Unexpected filename extension of file [" + url + "]. Should be .xml");
        }
    }

    public void autoConfig() throws JoranException {
        autoConfig(Configurator.class.getClassLoader());
    }

    public void autoConfig(ClassLoader classLoader) throws JoranException {
        String versionStr = EnvUtil.logbackVersion();
        if (versionStr == null) {
            versionStr = CoreConstants.NA;
        }
        loggerContext.getStatusManager().add(new InfoStatus(CoreConstants.LOGBACK_CLASSIC_VERSION_MESSAGE + versionStr, loggerContext));
        StatusListenerConfigHelper.installIfAsked(loggerContext);
        List<Configurator> configuratorList = ClassicEnvUtil.loadFromServiceLoader(Configurator.class, classLoader);
        sortByPriority(configuratorList);

        // this should never happen as we do have DefaultJoranConfigurator shipping with logback-classic
        if (configuratorList == null) {
            fallbackOnToBasicConfigurator();
            return;
        }
        for (Configurator c : configuratorList) {
            try {
                c.setContext(loggerContext);
                Configurator.ExecutionStatus status = c.configure(loggerContext);
                if (status == Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY) {
                    return;
                }
            } catch (Exception e) {
                throw new LogbackException(
                        String.format("Failed to initialize Configurator: %s using ServiceLoader",
                                c != null ? c.getClass().getCanonicalName() : "null"),
                        e);
            }
        }
        // at this stage invoke basicConfigurator
        fallbackOnToBasicConfigurator();

    }

    private void fallbackOnToBasicConfigurator() {
        BasicConfigurator basicConfigurator = new BasicConfigurator();
        basicConfigurator.setContext(loggerContext);
        basicConfigurator.configure(loggerContext);
    }

    private void sortByPriority(List<Configurator> configuratorList) {
        configuratorList.sort(new Comparator<Configurator>() {
            @Override
            public int compare(Configurator o1, Configurator o2) {
                if (o1.getClass() == o2.getClass())
                    return 0;
                if (o1 instanceof DefaultJoranConfigurator) {
                    return 1;
                }

                // otherwise do not intervene
                return 0;
            }
        });
    }


}
