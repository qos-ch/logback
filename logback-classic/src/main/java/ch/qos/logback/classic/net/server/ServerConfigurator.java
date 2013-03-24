/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.net.server;

import ch.qos.logback.classic.net.ssl.KeyManagerFactoryFactoryBean;
import ch.qos.logback.classic.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.classic.net.ssl.SSLConfiguration;
import ch.qos.logback.classic.net.ssl.SSLParametersFactoryBean;
import ch.qos.logback.classic.net.ssl.SecureRandomFactoryBean;
import ch.qos.logback.classic.net.ssl.TrustManagerFactoryFactoryBean;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

/**
 * A configurator for a socket server.
 * <p>
 * This configurator extends the {@link GenericConfigurator} to provide
 * rules for interpreting the server configuration format.
 * 
 * @author Carl Harris
 */
public class ServerConfigurator extends GenericConfigurator {

  private final ServerConfiguration config = new ServerConfiguration();
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("server"), new ServerAction(config));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    // The following line adds the capability to parse nested components
    NestedComplexPropertyIA nestedComplexPropertyIA = new NestedComplexPropertyIA();
    nestedComplexPropertyIA.setContext(context);
    interpreter.addImplicitAction(nestedComplexPropertyIA);

    NestedBasicPropertyIA nestedBasicIA = new NestedBasicPropertyIA();
    nestedBasicIA.setContext(context);
    interpreter.addImplicitAction(nestedBasicIA);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    registry.add(ServerConfiguration.class, "listener", 
        ListenerConfiguration.class);
    registry.add(ServerConfiguration.class, "ssl", 
        SSLConfiguration.class);
    registry.add(SSLConfiguration.class, "parameters", 
        SSLParametersFactoryBean.class);
    registry.add(SSLConfiguration.class, "keyStore", 
        KeyStoreFactoryBean.class);
    registry.add(SSLConfiguration.class, "keyManagerFactory", 
        KeyManagerFactoryFactoryBean.class);
    registry.add(SSLConfiguration.class, "trustStore", 
        KeyStoreFactoryBean.class);
    registry.add(SSLConfiguration.class, "trustManagerFactory", 
        TrustManagerFactoryFactoryBean.class);
    registry.add(SSLConfiguration.class, "secureRandom", 
        SecureRandomFactoryBean.class);
  }

  /**
   * Gets the configuration that will be loaded by the receiver on a
   * call to {@link #doConfigure(org.xml.sax.InputSource)} (or any of
   * its overloads).
   * @return configuration object
   */
  public ServerConfiguration getConfiguration() {
    return config;
  }

}
