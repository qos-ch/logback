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
package ch.qos.logback.classic.model.processor;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.joran.util.ParentTag_Tag_Class_Tuple;
import ch.qos.logback.core.net.ssl.SSLNestedComponentRegistryRules;
import ch.qos.logback.core.net.ssl.KeyManagerFactoryFactoryBean;
import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.SSLConfiguration;
import ch.qos.logback.core.net.ssl.SSLParametersConfiguration;
import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;
import ch.qos.logback.core.net.ssl.TrustManagerFactoryFactoryBean;

/**
 * Contains mappings for the default type of nested components in
 * logback-classic.
 * 
 * @author Ceki Gulcu
 * 
 */
public class LogbackClassicDefaultNestedComponentRules {

    static public List<ParentTag_Tag_Class_Tuple> TUPLES_LIST = createTuplesList();

    static public void addDefaultNestedComponentRegistryRules(DefaultNestedComponentRegistry registry) {
        registry.add(AppenderBase.class, "layout", PatternLayout.class);
        registry.add(UnsynchronizedAppenderBase.class, "layout", PatternLayout.class);

        registry.add(AppenderBase.class, "encoder", PatternLayoutEncoder.class);
        registry.add(UnsynchronizedAppenderBase.class, "encoder", PatternLayoutEncoder.class);

        registry.add(EvaluatorFilter.class, "evaluator", JaninoEventEvaluator.class);

        SSLNestedComponentRegistryRules.addDefaultNestedComponentRegistryRules(registry);
    }

    public static List<ParentTag_Tag_Class_Tuple> createTuplesList() {

        List<ParentTag_Tag_Class_Tuple> tupleList = new ArrayList<>();

        tupleList.add(new ParentTag_Tag_Class_Tuple("appender", "encoder", PatternLayoutEncoder.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("appender", "layout", PatternLayout.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("receiver", "ssl", SSLConfiguration.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("ssl", "parameters", SSLParametersConfiguration.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("ssl", "keyStore", KeyStoreFactoryBean.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("ssl", "trustStore", KeyManagerFactoryFactoryBean.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("ssl", "keyManagerFactory", SSLParametersConfiguration.class.getName()));
        tupleList
                .add(new ParentTag_Tag_Class_Tuple("ssl", "trustManagerFactory", TrustManagerFactoryFactoryBean.class.getName()));
        tupleList.add(new ParentTag_Tag_Class_Tuple("ssl", "secureRandom", SecureRandomFactoryBean.class.getName()));
        return tupleList;

    }



}
