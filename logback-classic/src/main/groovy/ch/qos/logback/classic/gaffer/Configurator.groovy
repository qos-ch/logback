/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
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
package ch.qos.logback.classic.gaffer

class Configurator {

  static void main(String[] args) {
    runArchitectureRules(new File("src/ch/test/toto.groovy"))
  }

  static void runArchitectureRules(File dsl) {
    //LoggerFactory loggerFactory = new LoggerFactory();
    ConfigurationDelegate configurationDelegate = new ConfigurationDelegate();
    //AppenderAction appenderAction = new AppenderAction();


    Binding binding = new Binding();
    binding.setProperty("DEBUG", new Integer(1));
    Script dslScript = new GroovyShell(binding).parse(dsl.text)
    ExpandoMetaClass emc = new ExpandoMetaClass(dslScript.class, false);

    //configurationDelegate.metaClass.logger = loggerFactory.&logger
    //configurationDelegate.metaClass.appender = appenderAction.&appender

    emc.configuration = {Closure cl ->
      println "executing configuration"
      cl.delegate = configuration
      cl();
    }


    emc.initialize();
    dslScript.metaClass = emc;

    dslScript.run()
  }

}