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
import ch.qos.logback.core.CoreConstants

class GafferConfigurator {

  LoggerContext context
  //ConfigurationDelegate configurationDelegate = new ConfigurationDelegate();
                         
  GafferConfigurator(LoggerContext context) {
    this.context = context
    //configurationDelegate.context = context;
  }

  protected void informContextOfURLUsedForConfiguration(URL url) {
    context.putObject(CoreConstants.URL_OF_LAST_CONFIGURATION_VIA_JORAN, url);
  }

  void run(URL url) {
    informContextOfURLUsedForConfiguration(url);
    run(url.text);
  }

  void run(File file) {
    informContextOfURLUsedForConfiguration(file.toURI().toURL());
    run(file.text);
  }

  void run(String dslText) {
    Binding binding = new Binding();
    binding.setProperty("hostname", ContextUtil.getLocalHostName());
    Script dslScript = new GroovyShell(binding).parse(dslText)

    dslScript.metaClass.mixin(ConfigurationDelegate)
    dslScript.setContext(context)
    dslScript.metaClass.getDeclaredOrigin = { dslScript }
//    metaClass.statusListener = configurationDelegate.&statusListener
//    dslScript.metaClass.scan = configurationDelegate.&scan
//    dslScript.metaClass.appender = configurationDelegate.&appender
//    dslScript.metaClass.root = configurationDelegate.&root
//    dslScript.metaClass.logger = configurationDelegate.&logger

    dslScript.run()
  }

}