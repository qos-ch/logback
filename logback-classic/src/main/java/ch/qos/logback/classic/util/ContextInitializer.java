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

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.VersionUtil;

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

    /**
     *  @deprecated Please use ClassicConstants.AUTOCONFIG_FILE instead
     */
    final public static String AUTOCONFIG_FILE = ClassicConstants.AUTOCONFIG_FILE;
    /**
     * @deprecated Please use ClassicConstants.TEST_AUTOCONFIG_FILE instead
     */
    final public static String TEST_AUTOCONFIG_FILE = ClassicConstants.TEST_AUTOCONFIG_FILE;
    /**
     * @deprecated Please use ClassicConstants.CONFIG_FILE_PROPERTY instead
     */
    final public static String CONFIG_FILE_PROPERTY = ClassicConstants.CONFIG_FILE_PROPERTY;

    String[] INTERNAL_CONFIGURATOR_CLASSNAME_LIST = {"ch.qos.logback.classic.util.DefaultJoranConfigurator", "ch.qos.logback.classic.BasicConfigurator"};

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

        // see https://github.com/qos-ch/logback/issues/715
        classLoader = Loader.systemClassloaderIfNull(classLoader);

        checkVersions();

        StatusListenerConfigHelper.installIfAsked(loggerContext);


        // invoke custom configurators
        List<Configurator> configuratorList = ClassicEnvUtil.loadFromServiceLoader(Configurator.class, classLoader);
        configuratorList.sort(rankComparator);
        if (configuratorList.isEmpty()) {
            contextAware.addInfo("No custom configurators were discovered as a service.");
        } else {
            printConfiguratorOrder(configuratorList);
        }

        for (Configurator c : configuratorList) {
            if (invokeConfigure(c) == Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY)
                return;
        }

        // invoke internal configurators
        for (String configuratorClassName : INTERNAL_CONFIGURATOR_CLASSNAME_LIST) {
            contextAware.addInfo("Trying to configure with "+configuratorClassName);
            Configurator c = instantiateConfiguratorByClassName(configuratorClassName, classLoader);
            if(c == null)
                continue;
            if (invokeConfigure(c) == Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY)
                return;
        }
    }

    private void checkVersions() {
        try {
            String coreVersion = VersionUtil.getCoreVersionBySelfDeclaredProperties();
            String classicVersion = ClassicVersionUtil.getVersionBySelfDeclaredProperties();
            VersionUtil.checkForVersionEquality(loggerContext, coreVersion, classicVersion, "logback-core", "logback-classic");
        }  catch(NoClassDefFoundError e) {
            contextAware.addWarn("Missing ch.logback.core.util.VersionUtil class on classpath. The version of logback-core is probably older than 1.5.26.");
        } catch (NoSuchMethodError e) {
            contextAware.addWarn(e.toString());
            contextAware.addWarn("The version of logback-core is probably older than 1.5.26.");
        }
    }

    private Configurator instantiateConfiguratorByClassName(String configuratorClassName, ClassLoader classLoader) {
        try {
            Class<?> classObj = classLoader.loadClass(configuratorClassName);
            return (Configurator) classObj.getConstructor().newInstance();
        } catch (ReflectiveOperationException  e) {
            contextAware.addInfo("Instantiation failure: " + e.toString());
            return null;
        }
    }

    /**
     *
     * @param configurator
     * @return ExecutionStatus
     */
    private Configurator.ExecutionStatus invokeConfigure(Configurator configurator) {
        try {
            long start = System.currentTimeMillis();
            contextAware.addInfo("Constructed configurator of type " + configurator.getClass());
            configurator.setContext(loggerContext);
            Configurator.ExecutionStatus status = configurator.configure(loggerContext);
            printDuration(start, configurator, status);
            return status;

        } catch (Exception e) {
            throw new LogbackException(String.format("Failed to initialize or to run Configurator: %s",
                    configurator != null ? configurator.getClass().getCanonicalName() : "null"), e);
        }
    }

    private void printConfiguratorOrder(List<Configurator> configuratorList) {
        contextAware.addInfo("Here is a list of configurators discovered as a service, by rank: ");
        for(Configurator c: configuratorList) {
            contextAware.addInfo("  "+c.getClass().getName());
        }
        contextAware.addInfo("They will be invoked in order until ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY is returned.");
    }

    private void printDuration(long start, Configurator configurator, Configurator.ExecutionStatus executionStatus) {
        long end = System.currentTimeMillis();
        long diff = end - start;
        contextAware.addInfo( configurator.getClass().getName()+".configure() call lasted "+diff + " milliseconds. ExecutionStatus="+executionStatus);
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

    Comparator<Configurator> rankComparator = new Comparator<Configurator>() {
        @Override
        public int compare(Configurator c1, Configurator c2) {

            ConfiguratorRank r1 = c1.getClass().getAnnotation(ConfiguratorRank.class);
            ConfiguratorRank r2 = c2.getClass().getAnnotation(ConfiguratorRank.class);

            int value1 = r1 == null ? ConfiguratorRank.DEFAULT : r1.value();
            int value2 = r2 == null ? ConfiguratorRank.DEFAULT : r2.value();

            int result = compareRankValue(value1, value2);
            // reverse the result for high to low sort
            return (-result);
        }
    };

    private int compareRankValue(int value1, int value2) {
        if(value1 > value2)
            return 1;
        else if (value1 == value2)
            return 0;
        else return -1;

    }
}
