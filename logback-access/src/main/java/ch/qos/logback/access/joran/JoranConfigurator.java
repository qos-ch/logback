/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.joran;


import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.joran.action.ConfigurationAction;
import ch.qos.logback.access.joran.action.EvaluatorAction;
import ch.qos.logback.access.sift.SiftAction;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.joran.JoranConfiguratorBase;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.NOPAction;
import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
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
    
    rs.addRule(new Pattern("configuration/evaluator"), new EvaluatorAction());
  }

  @Override
  protected void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    registry.add(AppenderBase.class, "layout", PatternLayout.class);
  }

}
