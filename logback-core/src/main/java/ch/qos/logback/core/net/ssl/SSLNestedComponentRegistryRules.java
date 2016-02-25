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
package ch.qos.logback.core.net.ssl;

import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;

/**
 * Nested component registry rules for {@link SSLConfiguration} and its
 * components.
 *
 * @author Carl Harris
 */
public class SSLNestedComponentRegistryRules {

    static public void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        registry.add(SSLComponent.class, "ssl", SSLConfiguration.class);
        registry.add(SSLConfiguration.class, "parameters", SSLParametersConfiguration.class);
        registry.add(SSLConfiguration.class, "keyStore", KeyStoreFactoryBean.class);
        registry.add(SSLConfiguration.class, "trustStore", KeyStoreFactoryBean.class);
        registry.add(SSLConfiguration.class, "keyManagerFactory", KeyManagerFactoryFactoryBean.class);
        registry.add(SSLConfiguration.class, "trustManagerFactory", TrustManagerFactoryFactoryBean.class);
        registry.add(SSLConfiguration.class, "secureRandom", SecureRandomFactoryBean.class);
    }

}
