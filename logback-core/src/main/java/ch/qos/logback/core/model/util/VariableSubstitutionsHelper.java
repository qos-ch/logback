/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.model.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Helper methods to deal with properties/
 *
 * @since 1.5.1
 */
public class VariableSubstitutionsHelper extends ContextAwareBase implements PropertyContainer {

    protected Map<String, String> propertiesMap;

    public VariableSubstitutionsHelper(Context context) {
        this.setContext(context);
        this.propertiesMap = new HashMap<>();
    }

    public VariableSubstitutionsHelper(Context context, Map<String, String> otherMap) {
        this.setContext(context);
        this.propertiesMap = new HashMap<>(otherMap);
    }

    public String subst(String ref) {
        if (ref == null) {
            return null;
        }

        try {
            return OptionHelper.substVars(ref, this, context);
        } catch (ScanException | IllegalArgumentException e) {
            addError("Problem while parsing [" + ref + "]", e);
            return ref;
        }

    }

    /**
     * Add a property to the properties of this execution context. If the property
     * exists already, it is overwritten.
     */
    public void addSubstitutionProperty(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        // values with leading or trailing spaces are bad. We remove them now.
        value = value.trim();
        propertiesMap.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return propertiesMap.get(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(propertiesMap);
    }
}
