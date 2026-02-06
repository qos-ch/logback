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

package ch.qos.logback.classic.util;

import ch.qos.logback.classic.ClassicConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for retrieving version information for the "logback-classic" module.
 * This class provides functionality to read and parse self-declared properties files
 * containing version metadata specific to the logback-classic module.
 *
 * It includes methods to locate the version properties file, extract the version string
 * based on specified conventions, and return the retrieved information.
 */
public class ClassicVersionUtil {

    // Code copied from VersionUtil. It must be located in the encompassing module and cannot be
    // shared.
    //
    // Retrieving version information by self-declared properties solves the issue of collapsed
    // MANIFEST.MF files as encountered in fat-jars.
    //
    // this code further assumes that the properties file is located in the same package as the aClass
    // parameter.
    static String getVersionBySelfDeclaredProperties(Class<?> aClass, String moduleName) {
        Properties props = new Properties();
        // example propertiesFileName: logback-core-version.properties
        //
        String propertiesFileName = moduleName + "-version.properties";
        String propertyKey = moduleName+"-version";
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

    /**
     * Retrieves the version information for the "logback-classic" module based on its self-declared properties.
     * The method looks for a properties file named "logback-classic-version.properties" within the classpath,
     * reads its contents, and fetches the value associated with the "logback-classic-version" key.
     *
     * @return the version string of the "logback-classic" module if found, or null if the properties file or version
     *         key is not present or an error occurs while reading the properties file.
     *
     * @since 1.5.26
     */
    static public String getVersionBySelfDeclaredProperties() {
        return getVersionBySelfDeclaredProperties(ClassicConstants.class, "logback-classic");
    }

}
