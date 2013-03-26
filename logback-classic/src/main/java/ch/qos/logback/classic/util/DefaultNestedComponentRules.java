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
package ch.qos.logback.classic.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.net.SSLSocketAppender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.net.ssl.KeyManagerFactoryFactoryBean;
import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.net.ssl.SSLParametersFactoryBean;
import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;
import ch.qos.logback.core.net.ssl.TrustManagerFactoryFactoryBean;

/**
 * Contains mappings for the default type of nested components in
 * logback-classic.
 * 
 * @author Ceki Gulcu
 * 
 */
public class DefaultNestedComponentRules {

  static public void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    registry.add(AppenderBase.class, "layout", PatternLayout.class);
    registry.add(UnsynchronizedAppenderBase.class, "layout", PatternLayout.class);
    
    registry.add(AppenderBase.class, "encoder", PatternLayoutEncoder.class);
    registry.add(UnsynchronizedAppenderBase.class, "encoder", PatternLayoutEncoder.class);
    
    registry
        .add(EvaluatorFilter.class, "evaluator", JaninoEventEvaluator.class);

    registry.add(SSLSocketAppender.class, "ssl", SSLConfiguration.class);
    
    registry.add(SSLConfiguration.class, "parameters", 
        SSLParametersFactoryBean.class);
    registry.add(SSLConfiguration.class, "keyStore", 
        KeyStoreFactoryBean.class);
    registry.add(SSLConfiguration.class, "trustStore", 
        KeyStoreFactoryBean.class);
    registry.add(SSLConfiguration.class, "keyManagerFactory", 
        KeyManagerFactoryFactoryBean.class);
    registry.add(SSLConfiguration.class, "trustManagerFactory", 
        TrustManagerFactoryFactoryBean.class);
    registry.add(SSLConfiguration.class, "secureRandom", 
        SecureRandomFactoryBean.class);
  }

}
