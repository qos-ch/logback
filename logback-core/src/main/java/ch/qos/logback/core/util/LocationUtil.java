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
package ch.qos.logback.core.util;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A static utility method that converts a string that describes the
 * location of a resource into a {@link URL} object.
 *
 * @author Carl Harris
 */
public class LocationUtil {

    /** Regex pattern for a URL scheme (reference RFC 2396 section 3) */
    public static final String SCHEME_PATTERN = "^\\p{Alpha}[\\p{Alnum}+.-]*:.*$";

    /** Scheme name for a classpath resource */
    public static final String CLASSPATH_SCHEME = "classpath:";

    /**
     * Converts a string describing the location of a resource into a URL object.
     * @param location String describing the location
     * @return URL object that refers to {@code location}
     * @throws MalformedURLException if {@code location} is not a syntatically
     *    valid URL
     * @throws FileNotFoundException if {@code location} specifies a non-existent
     *    classpath resource
     * @throws NullPointerException if {@code location} is {@code null}
     */
    public static URL urlForResource(String location) throws MalformedURLException, FileNotFoundException {
        if (location == null) {
            throw new NullPointerException("location is required");
        }
        URL url = null;
        if (!location.matches(SCHEME_PATTERN)) {
            url = Loader.getResourceBySelfClassLoader(location);
        } else if (location.startsWith(CLASSPATH_SCHEME)) {
            String path = location.substring(CLASSPATH_SCHEME.length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (path.length() == 0) {
                throw new MalformedURLException("path is required");
            }
            url = Loader.getResourceBySelfClassLoader(path);
        } else {
            url = new URL(location);
        }
        if (url == null) {
            throw new FileNotFoundException(location);
        }
        return url;
    }

}
