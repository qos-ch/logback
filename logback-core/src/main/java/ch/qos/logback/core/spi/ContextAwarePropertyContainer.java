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

package ch.qos.logback.core.spi;

import ch.qos.logback.core.joran.GenericXMLConfigurator;

import java.util.function.Supplier;

/**
 * An interface extending both {@link PropertyContainer} and {@link ContextAware}
 *
 * @since 1.5.1
 */
public interface ContextAwarePropertyContainer extends PropertyContainer, ContextAware {


    /**
     * This method is used tp perform variable substitution.
     *
     * @param input
     * @return a new string after variable substitution, if any.
     */
    String subst(String input);


    /**
     * Returns a supplier of {@link GenericXMLConfigurator} instance. The returned value may be null.
     *
     * <p>This method could/should have been part of a new interface. It is added here for reasons
     * of commodity and not coherence.</p>
     *
     * @return a supplier of {@link GenericXMLConfigurator} instance, may be null
     * @since 1.5.11
     */
    default public Supplier<? extends GenericXMLConfigurator> getConfiguratorSupplier() {
        return null;
    }
}
