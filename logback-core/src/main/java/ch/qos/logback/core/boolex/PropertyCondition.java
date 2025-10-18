/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2025, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.boolex;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.conditional.Condition;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.PropertyContainer;

/**
 * Interface for evaluating conditions based on properties during the conditional processing
 * of Logback configuration files. This interface is intended to provide an
 * alternative to legacy Janino-based evaluation.
 * <p>
 * Implementations of this interface can access both global properties from the {@link Context}
 * and local properties specific to the embedding configurator instance. This allows for fine-grained
 * and context-aware evaluation of configuration conditions.
 * </p>
 *
 * <p>
 * Typical usage involves implementing this interface to provide custom logic for evaluating
 * whether certain configuration blocks should be included or excluded based on property values.
 * </p>
 *
 * @since 1.5.20
 * @author Ceki G&uuml;lc&uuml;
 */
public interface PropertyCondition extends Condition, ContextAware, LifeCycle {

    /**
     * Returns the local {@link PropertyContainer} used for property lookups specific to the embedding configurator.
     * This is distinct from the global {@link Context} property container.
     *
     * @return the local property container, or null if not set
     */
    public PropertyContainer getLocalPropertyContainer();

    /**
     * Sets a {@link PropertyContainer} specific to the embedding configurator, which is used for property lookups
     * in addition to the global {@link Context} properties. This allows for overriding or supplementing global properties
     * with local values during evaluation.
     *
     * @param aPropertyContainer the local property container to use for lookups
     */
    public void setLocalPropertyContainer(PropertyContainer aPropertyContainer);


}