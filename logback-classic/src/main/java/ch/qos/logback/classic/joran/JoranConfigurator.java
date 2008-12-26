/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.joran.action.ConfigurationAction;
import ch.qos.logback.classic.joran.action.ConsolePluginAction;
import ch.qos.logback.classic.joran.action.ContextNameAction;
import ch.qos.logback.classic.joran.action.EvaluatorAction;
import ch.qos.logback.classic.joran.action.InsertFromJNDIAction;
import ch.qos.logback.classic.joran.action.JMXConfiguratorAction;
import ch.qos.logback.classic.joran.action.LayoutAction;
import ch.qos.logback.classic.joran.action.LevelAction;
import ch.qos.logback.classic.joran.action.LoggerAction;
import ch.qos.logback.classic.joran.action.RootLoggerAction;
import ch.qos.logback.classic.sift.SiftAction;
import ch.qos.logback.classic.spi.PlatformInfo;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.IncludeAction;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

/**
 * A JoranConfigurator add few rules specific to the Classic module.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class JoranConfigurator extends JoranConfiguratorBase {

  public JoranConfigurator() {
  }

  @Override
  public void addInstanceRules(RuleStore rs) {
    // parent rules already added
    super.addInstanceRules(rs);

    rs.addRule(new Pattern("configuration"), new ConfigurationAction());

    rs.addRule(new Pattern("configuration/contextName"), new ContextNameAction());
    rs.addRule(new Pattern("configuration/insertFromJNDI"), new InsertFromJNDIAction());
    rs.addRule(new Pattern("*/evaluator"), new EvaluatorAction());

    rs.addRule(new Pattern("configuration/appender/sift"), new SiftAction());
    rs.addRule(new Pattern("configuration/appender/sift/*"), new NOPAction());
    
    
    rs.addRule(new Pattern("configuration/logger"), new LoggerAction());
    rs.addRule(new Pattern("configuration/logger/level"), new LevelAction());

    rs.addRule(new Pattern("configuration/root"), new RootLoggerAction());
    rs.addRule(new Pattern("configuration/root/level"), new LevelAction());
    rs.addRule(new Pattern("configuration/logger/appender-ref"),
        new AppenderRefAction());
    rs.addRule(new Pattern("configuration/root/appender-ref"),
        new AppenderRefAction());
    rs
        .addRule(new Pattern("configuration/appender/layout"),
            new LayoutAction());
    
    // add jmxConfigurator only if we have JMX available.
    // If running under JDK 1.4 (retrotranslateed logback) then we
    // might not have JMX.
    if(PlatformInfo.hasJMXObjectName()) {
      rs.addRule(new Pattern("configuration/jmxConfigurator"), new JMXConfiguratorAction());
    }
    rs.addRule(new Pattern("configuration/include"), new IncludeAction());
    
    rs.addRule(new Pattern("configuration/consolePlugin"), new ConsolePluginAction());
  }

}
