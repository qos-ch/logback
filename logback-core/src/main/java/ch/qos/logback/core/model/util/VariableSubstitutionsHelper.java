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
import ch.qos.logback.core.spi.ContextAwarePropertyContainer;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods to deal with properties.
 *
 * <p>This class acts as a small container for substitution properties and
 * delegates actual variable substitution to {@link OptionHelper#substVars}.
 * It also offers a convenience method to mask confidential property values
 * (for example passwords) by returning a blurred placeholder.</p>
 *
 * @since 1.5.1
 */
public class VariableSubstitutionsHelper extends ContextAwareBase implements ContextAwarePropertyContainer {

    static final String PASSWORD = "password";
    static final String SECRET = "secret";
    static final String CONFIDENTIAL = "confidential";

    static final String BLURRED_STR = "******";

    protected Map<String, String> propertiesMap;

    /**
     * Create a helper backed by an empty property map.
     *
     * @param context the logback context to associate with this helper; may be null
     */
    public VariableSubstitutionsHelper(Context context) {
        this.setContext(context);
        this.propertiesMap = new HashMap<>();
    }

    /**
     * Create a helper pre-populated with the contents of {@code otherMap}.
     * The provided map is copied and further modifications do not affect the
     * original map.
     *
     * @param context the logback context to associate with this helper; may be null
     * @param otherMap initial properties to copy; if null an empty map is created
     */
    public VariableSubstitutionsHelper(Context context, Map<String, String> otherMap) {
        this.setContext(context);
        this.propertiesMap = new HashMap<>(otherMap);
    }

    /**
     * Perform variable substitution on the provided reference string.
     *
     * <p>Returns {@code null} if {@code ref} is {@code null}. On parse errors
     * the original input string is returned and an error is logged.</p>
     *
     * @param ref the string possibly containing variables to substitute
     * @return the string with substitutions applied, or {@code null} if {@code ref} was {@code null}
     */
    @Override
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
     * Return a blurred placeholder for confidential properties.
     *
     * <p>If the property name {@code ref} contains any of the case-insensitive
     * substrings {@code "password"}, {@code "secret"} or {@code "confidential"}
     * this method returns a fixed blurred string ("******"). Otherwise, the
     * supplied {@code substituted} value is returned unchanged.</p>
     *
     * @param ref the property name to inspect; must not be {@code null}
     * @param substituted the substituted value to return when the property is not confidential
     * @return a blurred placeholder when the property appears confidential, otherwise {@code substituted}
     * @throws IllegalArgumentException when {@code ref} is {@code null}
     */
    public String sanitizeIfConfidential(String ref, String substituted) {
        if(ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }

        String lowerCaseRef = ref.toLowerCase();

        if(lowerCaseRef.contains(PASSWORD) || lowerCaseRef.contains(SECRET) || lowerCaseRef.contains(CONFIDENTIAL)) {
            return BLURRED_STR;
        } else
            return substituted;
    }

    /**
     * Add or overwrite a substitution property.
     *
     * <p>Null keys or values are ignored. Values are trimmed before storing
     * to avoid surprises caused by leading or trailing whitespace.</p>
     *
     * @param key the property name; ignored if {@code null}
     * @param value the property value; ignored if {@code null}
     */
    @Override
    public void addSubstitutionProperty(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        // values with leading or trailing spaces are bad. We remove them now.
        value = value.trim();
        propertiesMap.put(key, value);
    }

    /**
     * Retrieve a property value by name.
     *
     * @param key the property name
     * @return the property value or {@code null} if not present
     */
    @Override
    public String getProperty(String key) {
        return propertiesMap.get(key);
    }

    /**
     * Return a shallow copy of the internal property map.
     *
     * <p>The returned map is a copy and modifications to it do not affect the
     * internal state of this helper.</p>
     *
     * @return a copy of the property map
     */
    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<>(propertiesMap);
    }
}
