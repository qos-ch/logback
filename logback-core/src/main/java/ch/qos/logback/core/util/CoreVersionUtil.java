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

import ch.qos.logback.core.CoreConstants;

/**
 * Utility class for retrieving version information of the "logback-core" module.
 *
 * @since 1.5.26
 */
public class CoreVersionUtil {
    /**
     * Retrieves the version of the "logback-core" module using a properties file
     * associated with the module.
     *
     * <p>The method locates and reads a properties file named "logback-core-version.properties"
     * in the package of the {@code CoreConstants.class}. It then extracts the version
     * information using the key "logback-core-version".
     * </p>
     *
     * @return the version of the "logback-core" module as a string, or null if the version cannot be determined
     * @since 1.5.26
     */
    static public String getCoreVersionBySelfDeclaredProperties() {
        return VersionUtil.getArtifactVersionBySelfDeclaredProperties(CoreConstants.class, "logback-core");
    }
}
