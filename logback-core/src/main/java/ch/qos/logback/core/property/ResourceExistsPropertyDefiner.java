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
package ch.qos.logback.core.property;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.net.URL;

/**
 * In conjunction with {@link ch.qos.logback.core.joran.action.PropertyAction} sets
 * the named variable to "true" if the {@link #setResource(String) resource} specified
 * by the user is available on the class path, "false" otherwise.
 *
 * @see #getPropertyValue()
 *
 * @author XuHuisheng
 * @author Ceki Gulcu
 * @since 1.1.0
 */
public class ResourceExistsPropertyDefiner extends PropertyDefinerBase {

    String resourceStr;

    public String getResource() {
        return resourceStr;
    }

    /**
     * The resource to search for on the class path.
     *
     * @param resource
     */
    public void setResource(String resource) {
        this.resourceStr = resource;
    }

    /**
     * Returns the string "true" if the {@link #setResource(String) resource} specified by the
     * user is available on the class path, "false" otherwise.
     *
     * @return "true"|"false" depending on the availability of resource on the classpath
     */
    public String getPropertyValue() {
        if (OptionHelper.isNullOrEmpty(resourceStr)) {
            addError("The \"resource\" property must be set.");
            return null;
        }

        URL resourceURL = Loader.getResourceBySelfClassLoader(resourceStr);
        return booleanAsStr(resourceURL != null);
    }

}
