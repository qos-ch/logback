/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.util.Optional;
import java.util.Properties;

import static ch.qos.logback.core.CoreConstants.NA;



/**
 * Utility class for handling and validating version information of various artifacts.
 *
 * <p>It is used by logback-classic, logback-access-common, logback-access-jetty11, logback-access-tomcat, etc.
 * to alert users about version discrepancies between dependent and dependee artifacts.
 * </p>
 *
 * @since 1.5.25
 */
public class VersionUtil {

    /**
     * Retrieves the version of an artifact, such as logback-core.jar, logback-access-common.jar etc.
     *
     * <p>The aClass parameter is assumed to be part of the artifact.
     * </p>
     *
     * <p>The method first attempts to get the version from the module information. If the module version
     * is not available, it falls back to retrieving the implementation version from the package.
     * </p>
     *
     * @param aClass the class from which to retrieve the version information
     * @return the version of the artifact where aClass is found, or null if the version cannot be determined
     * @since 2.0.9
     */
    static public String getVersionOfArtifact(Class<?> aClass) {
        String moduleVersion = getVersionOfClassByModule(aClass);
        if (moduleVersion != null)
            return moduleVersion;

        Package pkg = aClass.getPackage();
        if (pkg == null) {
            return null;
        }
        return pkg.getImplementationVersion();
    }

    static public String nonNull(String input) {
        if (input == null) {
            return NA;
        } else {
            return input;
        }
    }

    /**
     * Retrieves the version of an artifact from the artifact's module metadata.
     *
     * <p>If the module or its descriptor does not provide a version, the method returns null.
     * </p>
     *
     * @param aClass a class from which to retrieve the version information
     * @return the version of class' module as a string, or null if the version cannot be determined
     * @since 2.0.9
     */
    static private String getVersionOfClassByModule(Class<?> aClass) {
        Module module = aClass.getModule();
        if (module == null)
            return null;

        ModuleDescriptor md = module.getDescriptor();
        if (md == null)
            return null;
        Optional<String> opt = md.rawVersion();
        return opt.orElse(null);
    }

    static String getExpectedVersionOfDependeeByProperties(Class<?> dependentClass, String propertiesFileName, String dependeeNameAsKey) {
        Properties props = new Properties();
        // propertiesFileName : logback-access-common-dependees.properties
        try (InputStream is = dependentClass.getClassLoader()
                .getResourceAsStream(propertiesFileName)) {
            if (is != null) {
                props.load(is);
                return props.getProperty(dependeeNameAsKey);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    static public void checkForVersionEquality(Context context, Class<?> dependentClass, Class<?> dependeeClass, String dependentName, String dependeeName) {
        // the dependent depends on the dependee
        String dependentVersion = nonNull(getVersionOfArtifact(dependentClass));
        String dependeeVersion = nonNull(getVersionOfArtifact(dependeeClass));

        addFoundVersionStatus(context, dependentName, dependentVersion);

        if (dependentVersion.equals(NA) || !dependentVersion.equals(dependeeVersion)) {
            addFoundVersionStatus(context, dependeeName, dependeeVersion);
            String discrepancyMsg = String.format("Versions of %s and %s are different!", dependeeName, dependentName);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));
        }
    }

    private static void addFoundVersionStatus(Context context, String name, String version) {
        String foundDependent = String.format("Found %s version %s", name, version);
        context.getStatusManager().add(new InfoStatus(foundDependent, context));
    }



    private static String nameToFilename(String  name) {
        return name+"-dependees.properties";
    }

    static public void compareExpectedAndFoundVersion(Context context, Class<?> dependentClass, Class<?> dependeeClass,
                                               String dependentName, String dependeeName) {

        String expectedDependeeVersion = nonNull(getExpectedVersionOfDependeeByProperties(dependentClass, nameToFilename(dependentName), dependeeName));
        String actualDependeeVersion = nonNull(getVersionOfArtifact(dependeeClass));
        String dependentVersion = nonNull(getVersionOfArtifact(dependentClass));

        addFoundVersionStatus(context, dependeeName, actualDependeeVersion);
        addFoundVersionStatus(context, dependentName, dependentVersion);

        if (!expectedDependeeVersion.equals(actualDependeeVersion)) {
            String discrepancyMsg = String.format("Expected version of %s is %s but found %s", dependeeName, expectedDependeeVersion, actualDependeeVersion);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));
        }

    }
}
