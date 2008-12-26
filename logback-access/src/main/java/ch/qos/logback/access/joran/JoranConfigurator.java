/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2007, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
 
package ch.qos.logback.access.joran;


import ch.qos.logback.access.joran.action.ConfigurationAction;
import ch.qos.logback.access.joran.action.EvaluatorAction;
import ch.qos.logback.access.sift.SiftAction;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;



/**
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class JoranConfigurator extends JoranConfiguratorBase {

  @Override
  public void addInstanceRules(RuleStore rs) {
    super.addInstanceRules(rs);
    
    rs.addRule(new Pattern("configuration"), new ConfigurationAction());
    rs.addRule(new Pattern("configuration/appender-ref"), new AppenderRefAction());
    
    rs.addRule(new Pattern("configuration/appender/sift"), new SiftAction());
    rs.addRule(new Pattern("configuration/appender/sift/*"), new NOPAction());
    
    rs.addRule(new Pattern("*/evaluator"), new EvaluatorAction());
  }


}
