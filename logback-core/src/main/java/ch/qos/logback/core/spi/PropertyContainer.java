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
package ch.qos.logback.core.spi;

import java.util.Map;
import java.util.Properties;

public interface PropertyContainer {


    void addSubstitutionProperty(String key, String value);

    String getProperty(String key);

    Map<String, String> getCopyOfPropertyMap();

    /**
     * Returns true if the value of the property named <code>key</code> is to be
     * used verbatim, i.e. without being subjected to variable substitution
     * itself. Properties holding data obtained at runtime, such as the
     * discriminating value of a sifting appender, are literal. By default, no
     * property is literal.
     *
     * @param key the property name
     * @return true if the value of the property is literal
     * @since 1.7.0
     */
    default boolean isLiteralProperty(String key) {
        return false;
    }

    default void addSubstitutionProperties(Properties props) {
        if (props == null) {
            return;
        }
        props.forEach((k, v) -> addSubstitutionProperty((String) k, (String) v));
    }
}
