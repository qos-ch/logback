/**
 * Logback: the reliable, generic, fast and flexible logging framework. Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under either the terms of the Eclipse Public License v1.0 as published by the Eclipse
 * Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

import java.util.Comparator;
import java.util.List;

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

    final ContextAware contextAware;

    public ContextInitializer(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        this.contextAware = new ContextAwareImpl(loggerContext, this);
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

        long startConfigurationAsAService = System.currentTimeMillis();
        List<Configurator> configuratorList = ClassicEnvUtil.loadFromServiceLoader(Configurator.class, classLoader);

        configuratorList.sort(rankComparator);

        for (Configurator c : configuratorList) {
            try {
                contextAware.addInfo("Constructed configurator of type " + c.getClass());
                c.setContext(loggerContext);
                Configurator.ExecutionStatus status = c.configure(loggerContext);
                if (status == Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY) {
                    printDuration(startConfigurationAsAService, "Configuration as a service duration ", true);
                    return;
                }
            } catch (Exception e) {
                throw new LogbackException(String.format("Failed to initialize Configurator: %s using ServiceLoader",
                        c != null ? c.getClass().getCanonicalName() : "null"), e);
            }
        }

        printDuration(startConfigurationAsAService, "Configuration as a service duration ", false);

        long startJoranConfiguration = System.currentTimeMillis();
        Configurator.ExecutionStatus es = attemptConfigurationUsingJoranUsingReflexion(classLoader);

        if (es == Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY) {
            printDuration(startJoranConfiguration, "JoranConfiguration duration", true);
            return;
        }
        printDuration(startJoranConfiguration, "JoranConfiguration duration", false);

        // at this stage invoke basicConfigurator
        fallbackOnToBasicConfigurator();
    }

    private void printDuration(long start, String message, boolean success) {
        long end = System.currentTimeMillis();
        long configurationAsAServiceDuration = end - start;
        contextAware.addInfo(message+configurationAsAServiceDuration + " milliseconds. Success status="+success);
    }

    private Configurator.ExecutionStatus attemptConfigurationUsingJoranUsingReflexion(ClassLoader classLoader) {

        try {
            Class<?> djcClass = classLoader.loadClass("ch.qos.logback.classic.util.DefaultJoranConfigurator");
            Configurator c = (Configurator) djcClass.newInstance();
            c.setContext(loggerContext);
            return c.configure(loggerContext);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            contextAware.addError("unexpected exception while instantiating DefaultJoranConfigurator", e);
            return Configurator.ExecutionStatus.INVOKE_NEXT_IF_ANY;
        }

    }

    private void fallbackOnToBasicConfigurator() {
        BasicConfigurator basicConfigurator = new BasicConfigurator();
        basicConfigurator.setContext(loggerContext);
        basicConfigurator.configure(loggerContext);
    }

    //    private void sortByPriority(List<Configurator> configuratorList) {
    //        configuratorList.sort(new Comparator<Configurator>() {
    //            @Override
    //            public int compare(Configurator o1, Configurator o2) {
    //                if (o1.getClass() == o2.getClass())
    //                    return 0;
    //                if (o1 instanceof DefaultJoranConfigurator) {
    //                    return 1;
    //                }
    //
    //                // otherwise do not intervene
    //                return 0;
    //            }
    //        });
    //    }

    Comparator<Configurator> rankComparator = new Comparator<Configurator>() {
        @Override
        public int compare(Configurator c1, Configurator c2) {

            ConfiguratorRank r1 = c1.getClass().getAnnotation(ConfiguratorRank.class);
            ConfiguratorRank r2 = c2.getClass().getAnnotation(ConfiguratorRank.class);

            ConfiguratorRank.Value value1 = r1 == null ? ConfiguratorRank.Value.REGULAR : r1.value();
            ConfiguratorRank.Value value2 = r2 == null ? ConfiguratorRank.Value.REGULAR : r2.value();

            int result = compareRankValue(value1, value2);
            // reverse the result for high to low sort
            return (-result);
        }
    };

    private int compareRankValue(ConfiguratorRank.Value value1, ConfiguratorRank.Value value2) {

        switch (value1) {
        case FIRST:
            if (value2 == ConfiguratorRank.Value.FIRST)
                return 0;
            else
                return 1;
        case REGULAR:
            if (value2 == ConfiguratorRank.Value.FALLBACK)
                return 1;
            else if (value2 == ConfiguratorRank.Value.REGULAR)
                return 0;
            else
                return -1;
        case FALLBACK:
            if (value2 == ConfiguratorRank.Value.FALLBACK)
                return 0;
            else
                return -1;

        default:
            return 0;
        }

    }
}
