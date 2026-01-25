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

package ch.qos.logback.core.boolex;

import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

/**
 * <p>Abstract base class provides some scaffolding. It is intended to ease migration
 * from <b>legacy</b> conditional processing in configuration files
 * (e.g. &lt;if>, &lt;then&gt;, &lt;else>) using the Janino library. Nevertheless,
 * it should also be useful in newly written code.</p>
 *
 * <p>Properties are looked up in the following order:</p>
 *
 * <ol>
 *  <li>In the local property container, usually the {@link ModelInterpretationContext} </li>
 *  <li>in the logger context</li>
 *  <li>system properties</li>
 *  <li>environment variables</li>
 * </ol>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @see OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)
 * @since 1.5.20
 */
abstract public class PropertyConditionBase extends ContextAwareBase implements PropertyCondition {

    /**
     * Indicates whether this evaluator has been started.
     */
    boolean started;
    /**
     * <p>The local property container used for property lookups.</p>
     *
     * <p>Local properties correspond to the properties in the embedding
     * configurator, i.e. usually the {@link ModelInterpretationContext} instance.</p>
     */
    PropertyContainer localPropertyContainer;

    /**
     * Returns the local property container used by this evaluator.
     *
     * <p>Local properties correspond to the properties in the embedding
     * configurator, i.e. usually the {@link ModelInterpretationContext} instance.</p>
     *
     * @return the local property container
     */
    @Override
    public PropertyContainer getLocalPropertyContainer() {
        return localPropertyContainer;
    }

    /**
     * Sets the local property container for this evaluator.
     *
     * <p>Local properties correspond to the properties in the embedding
     * configurator, i.e. usually the {@link ModelInterpretationContext} instance.</p>
     *
     * @param aLocalPropertyContainer the local property container to set
     */
    @Override
    public void setLocalPropertyContainer(PropertyContainer aLocalPropertyContainer) {
        this.localPropertyContainer = aLocalPropertyContainer;
    }

    /**
     * Checks if the property with the given key is null.
     *
     * <p>The property is looked up via the
     * {@link OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)} method.
     * See above for the lookup order.</p>
     *
     * @param k the property key
     * @return true if the property is null, false otherwise
     */
    public boolean isNull(String k) {
        String val = OptionHelper.propertyLookup(k, localPropertyContainer, getContext());
        return (val == null);
    }

    /**
     * Checks if the property with the given key is defined (not null).
     *
     * <p>The property is looked up via the
     * {@link OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)} method.
     * See above for the lookup order.</p>
     *
     * @param k the property key
     * @return true if the property is defined, false otherwise
     */
    public boolean isDefined(String k) {
        String val = OptionHelper.propertyLookup(k, localPropertyContainer, getContext());
        return (val != null);
    }

    /**
     * Retrieves the property value for the given key, returning an empty string if null.
     * This is a shorthand for {@link #property(String)}.
     *
     * @param k the property key
     * @return the property value or an empty string
     */
    public String p(String k) {
        return property(k);
    }

    /**
     * Retrieves the property value for the given key, returning an empty string if null.
     *
     * <p>The property is looked up via the
     * {@link OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)} method.
     * See above for the lookup order.</p>
     *
     * @param k the property key
     * @return the property value or an empty string
     */
    public String property(String k) {
        String val = OptionHelper.propertyLookup(k, localPropertyContainer, getContext());
        if (val != null)
            return val;
        else
            return EMPTY_STRING;
    }

    /**
     * Compare the resolved property value with the provided expected value.
     *
     * <p>The property is looked up via the
     * {@link OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)} method.
     * See above for the lookup order.</p>
     *
     * <p>Returns {@code true} if the resolved property value is equal to {@code val}
     * according to {@link String#equals(Object)}. If the resolved property value or {@code val} is null,
     * then false is returned.</p>
     *
     * @param propertyKey the property key to look up
     * @param value       expected string value to compare against; must be non-null
     * @return {@code true} if the resolved property equals {@code value},
     * {@code false} otherwise or if either the resolved property or {@code value} is null.
     * @since 1.5.24
     */
    public boolean propertyEquals(String propertyKey, String value) {
        String actual = OptionHelper.propertyLookup(propertyKey, localPropertyContainer, getContext());
        if (actual == null || value == null) {
            return false;
        }
        return actual.equals(value);
    }


    /**
     * Determine whether the resolved property value contains the given substring.
     * <p>
     *
     * <p>The property is looked up via the
     * {@link OptionHelper#propertyLookup(String, PropertyContainer, PropertyContainer)} method.
     * See above for the lookup order.</p>
     *
     * <p>This method returns {@code true} if the resolved property value's
     * {@link String#contains(CharSequence)} returns {@code true} for the supplied
     * {@code inclusion}. False is returned if either the resolved property value or
     * {@code inclusion} parameter is null.</p>
     *
     * @param k         the property key to look up
     * @param inclusion substring to search for in the resolved property value; must be non-null
     * @return {@code true} if the property value contains {@code inclusion}, false otherwise or
     * if either the resolved property value or {@code inclusion} is null
     *
     * @since 1.5.24
     */
    public boolean propertyContains(String k, String inclusion) {
        String actual = OptionHelper.propertyLookup(k, localPropertyContainer, getContext());
        if (actual == null || inclusion == null)
            return false;

        return actual.contains(inclusion);
    }

    /**
     * Checks if this evaluator has been started.
     *
     * @return true if started, false otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Starts this evaluator.
     */
    public void start() {
        started = true;
    }

    /**
     * Stops this evaluator.
     */
    public void stop() {
        started = false;
    }
}
