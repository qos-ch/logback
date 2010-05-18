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

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.ContextUtil

class Configurator {

  LoggerContext context
  //ConfigurationDelegate configurationDelegate = new ConfigurationDelegate();

  Configurator(LoggerContext context) {
    this.context = context
    //configurationDelegate.context = context;
  }

  void run(String dslText) {
    Binding binding = new Binding();
    binding.setProperty("hostname", ContextUtil.getLocalHostName());
    Script dslScript = new GroovyShell(binding).parse(dslText)
    
    dslScript.metaClass.mixin(ConfigurationDelegate)
    dslScript.setContext(context)
    dslScript.metaClass.getDeclaredOrigin = { println "getDeclaredOrigin"; dslScript }
//    metaClass.statusListener = configurationDelegate.&statusListener
//    dslScript.metaClass.scan = configurationDelegate.&scan
//    dslScript.metaClass.appender = configurationDelegate.&appender
//    dslScript.metaClass.root = configurationDelegate.&root
//    dslScript.metaClass.logger = configurationDelegate.&logger

    dslScript.run()
  }

}