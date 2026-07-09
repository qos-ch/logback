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

import java.lang.module.ModuleDescriptor;
import java.util.Optional;

import static ch.qos.logback.core.CoreConstants.CODES_URL;
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

    static final String DEPENDER_S_WAS_EXPECTING_PATTERN = "Depender [%s] was expecting version %s for dependency [%s] but found version %s";
    /**
     * @since 1.5.30
     */
    Context context;

    /**
     * Instance methods allow for polymorphism, static methods do not.
     *
     *
     * @param context
     * @since 1.5.30
     */
    protected VersionUtil(Context context) {
        this.context = context;
    }

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

   protected String getExpectedVersionOfDependencyByProperties(Class<?> dependerClass, String propertiesFileName, String dependencyNameAsKey) {
        // derived classes should override
        return null;
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


    protected static void addFoundVersionStatus(Context context, String name, String version) {
        String foundDependent = String.format("Found %s version %s", name, nonNull(version));
        context.getStatusManager().add(new InfoStatus(foundDependent, context));
    }

    protected static String nameToPropertiesFilename(String name) {
        return name + "-dependencies.properties";
    }

    /**
     * Compares the expected version of a dependency with the actual version found and updates the status context.
     * If the versions do not match, a warning is added to the context's status manager.
     *
     * <p>Note: This method is used be logback-access-jetty11/12 and logback-access-tomcat.</p>
     *
     */
    public void compareExpectedAndFoundVersion(String actualDependencyVersion, Class<?> dependerClass, String dependerVersion,
                                                      String dependerName, String dependencyName) {

        String propertiesFileName = nameToPropertiesFilename(dependerName);

        String expectedDependencyVersion = this.getExpectedVersionOfDependencyByProperties(dependerClass, propertiesFileName, dependencyName);
        String safeExpectedDependencyVersion = nonNull(expectedDependencyVersion);

        addFoundVersionStatus(context, dependencyName, actualDependencyVersion);
        addFoundVersionStatus(context, dependerName, dependerVersion);

        if (!safeExpectedDependencyVersion.equals(actualDependencyVersion)) {
            String discrepancyMsg = String.format(DEPENDER_S_WAS_EXPECTING_PATTERN, dependerName,
                    safeExpectedDependencyVersion, dependencyName, actualDependencyVersion);
            context.getStatusManager().add(new WarnStatus(discrepancyMsg, context));

            String seeMsg = "See also "+CODES_URL+"#versionMismatch";
            context.getStatusManager().add(new WarnStatus(seeMsg, context));

        }
    }
}
