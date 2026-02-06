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

// depender depends on dependency

// dependency synonym dependee (only use dependency)
// depender synonym dependent (only use depender)

/**
 * Utility class for handling and validating version information of various artifacts.
 *
 * <p>It is used by logback-classic, logback-access-common, logback-access-jetty11, logback-access-tomcat, etc.
 * to alert users about version discrepancies between depender and dependency artifacts.
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
     * @deprecated
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

    /**
     * Retrieves the version of a module using a properties file associated with the module.
     *
     * <p>Unfortunately, this code cannot be called by other modules. It needs to be duplicated.</p>
     *
     * <p>The method looks for a properties file with a name derived from the <code>moduleName</code> parameter,
     * in the same location, e.g. package, as the <code>aClass</code> parameter. It attempts to load the properties file
     * and fetch the version information using a specific key.
     * </p>
     *
     * <p>The properties file is expected to be in the same package as the class provided, and named
     * <code>moduleName-version.properties</code>. The properties file should contain a single key-value pair,
     * where the key is <code>moduleName-version</code>, and the value is the module version.
     *
     * @param aClass     the class used to locate the resource file, the properties file is expected to be in the same package
     * @param moduleName the name of the module, which is used to construct the properties file name and the key
     * @return the version of the module as a string, or null if the version cannot be determined
     * @since 1.5.26
     * @deprecated (this code cannot be shared and is useless here)
     */
    static public String getArtifactVersionBySelfDeclaredProperties(Class<?> aClass, String moduleName) {
        Properties props = new Properties();
        // example propertiesFileName: logback-core-version.properties
        //
        String propertiesFileName = moduleName + "-version.properties";
        String propertyKey = moduleName + "-version";
        try (InputStream is = aClass.getResourceAsStream(propertiesFileName)) {
            if (is != null) {
                props.load(is);
                return props.getProperty(propertyKey);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }




    static String getExpectedVersionOfDependencyByProperties(Class<?> dependerClass, String propertiesFileName, String dependencyNameAsKey) {
        Properties props = new Properties();
        // propertiesFileName : logback-access-common-dependencies.properties
        try (InputStream is = dependerClass.getClassLoader()
                .getResourceAsStream(propertiesFileName)) {
            if (is != null) {
                props.load(is);
                return props.getProperty(dependencyNameAsKey);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }


    static public void checkForVersionEquality(Context context, Class<?> dependerClass, Class<?> dependencyClass, String dependerName, String dependencyName) {
        // the depender depends on the dependency
        String dependerVersion = nonNull(getVersionOfArtifact(dependerClass));
        String dependencyVersion = nonNull(getVersionOfArtifact(dependencyClass));

        checkForVersionEquality(context, dependerVersion, dependencyVersion, dependerName, dependencyName);
    }

    static public void checkForVersionEquality(Context context, Class<?> dependerClass, String dependencyVersion, String dependerName, String dependencyName) {
        String dependerVersion = nonNull(getVersionOfArtifact(dependerClass));
        checkForVersionEquality(context, dependerVersion, dependencyVersion, dependerName, dependencyName);
    }


    /**
     * Compares the versions of a depender and a dependency to determine if they are equal.
     * Updates the context's status manager with version information and logs a warning
     * if the versions differ.
     *
     * @since 1.5.26
     */
    static public void checkForVersionEquality(Context context, String dependerVersion, String dependencyVersion, String dependerName, String dependencyName) {
        // the depender depends on the dependency
        addFoundVersionStatus(context, dependerName, dependerVersion);

        dependerVersion = nonNull(dependerVersion);

        if (dependerVersion.equals(NA) || !dependerVersion.equals(dependencyVersion)) {
            addFoundVersionStatus(context, dependencyName, dependencyVersion);
            String discrepancyMsg = String.format("Versions of %s and %s are different or unknown.", dependencyName, dependerVersion);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));
        }
    }


    private static void addFoundVersionStatus(Context context, String name, String version) {
        String foundDependent = String.format("Found %s version %s", name, nonNull(version));
        context.getStatusManager().add(new InfoStatus(foundDependent, context));
    }

    private static String nameToPropertiesFilename(String name) {
        return name + "-dependencies.properties";
    }

    /**
     * Compares the expected version of a dependency with the actual version found and updates the status context.
     * If the versions do not match, a warning is added to the context's status manager.
     *
     * <p>Note: This method is used be logback-access-jetty11/12 and logback-access-tomcat.</p>
     *
     */
    static public void compareExpectedAndFoundVersion(Context context, String actualDependencyVersion, Class<?> dependerClass, String dependerVersion,
                                                      String dependerName, String dependencyName) {

        String expectedDependencyVersion = nonNull(getExpectedVersionOfDependencyByProperties(dependerClass, nameToPropertiesFilename(dependerName), dependencyName));

        addFoundVersionStatus(context, dependencyName, actualDependencyVersion);
        addFoundVersionStatus(context, dependerName, dependerVersion);

        if (!expectedDependencyVersion.equals(actualDependencyVersion)) {
            String discrepancyMsg = String.format("Expected version of %s is %s but found %s", dependencyName, expectedDependencyVersion, actualDependencyVersion);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));
        }
    }
}
