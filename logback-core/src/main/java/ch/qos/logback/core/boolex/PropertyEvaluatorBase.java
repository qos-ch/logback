/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 *  Copyright (C) 1999-2025, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *     or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */

package ch.qos.logback.core.boolex;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.PropertyContainer;
import ch.qos.logback.core.util.OptionHelper;

/**
 * <p>Abstract base class provides some scaffolding. it is intended to ease migration
 * from <b>legacy</b> conditional processing in configuration files
 * (e.g. &lt;if>, &lt;then>, &lt;else>) using the Janino library.
 * </p>
 *
 * <p>Nevertheless, it should also be useful in newly written code. </p>
 *
 * @since 1.5.20
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class PropertyEvaluatorBase extends ContextAwareBase implements PropertyEvaluator {

    /**
     * Indicates whether this evaluator has been started.
     */
    boolean started;
    /**
     * <p>The local property container used for property lookups.</p>
     *
     * <p>Local properties correspond to the properties in the embedding
     * configurator.</p>
     */
    PropertyContainer localPropertyContainer;

    /**
     * Returns the property container used by this evaluator.
     *
     * @return the property container
     */
    @Override
    public PropertyContainer getLocalPropertyContainer() {
        return localPropertyContainer;
    }

    /**
     * Sets the property container for this evaluator.
     *
     * @param aPropertyContainer the property container to set
     */
    @Override
    public void setLocalPropertyContainer(PropertyContainer aPropertyContainer) {
        this.localPropertyContainer = aPropertyContainer;
    }

    /**
     * Checks if the property with the given key is null.
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
     * @param k the property key
     * @return the property value or an empty string
     */
    public String property(String k) {
        String val = OptionHelper.propertyLookup(k, localPropertyContainer, getContext());
        if (val != null)
            return val;
        else
            return "";
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
