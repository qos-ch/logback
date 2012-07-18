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
package ch.qos.logback.core.joran;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.action.AppenderRefAction;
import ch.qos.logback.core.joran.action.ComponentFactoryAction;
import ch.qos.logback.core.joran.action.ContextPropertyAction;
import ch.qos.logback.core.joran.action.ConversionRuleAction;
import ch.qos.logback.core.joran.action.DefinePropertyAction;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.action.NewRuleAction;
import ch.qos.logback.core.joran.action.ParamAction;
import ch.qos.logback.core.joran.action.PropertyAction;
import ch.qos.logback.core.joran.action.StatusListenerAction;
import ch.qos.logback.core.joran.action.TimestampAction;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

// Based on 310985 revision 310985 as attested by http://tinyurl.com/8njps
// see also http://tinyurl.com/c2rp5

/**
 * A JoranConfiguratorBase lays most of the groundwork for concrete
 * configurators derived from it. Concrete configurators only need to implement
 * the {@link #addInstanceRules} method.
 * <p>
 * A JoranConfiguratorBase instance should not be used more than once to
 * configure a Context.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class JoranConfiguratorBase extends GenericConfigurator {

  public List getErrorList() {
    return null;
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {

    rs.addRule(new Pattern("configuration/property"), new PropertyAction());

    rs.addRule(new Pattern("configuration/substitutionProperty"),
        new PropertyAction());

    rs.addRule(new Pattern("configuration/timestamp"), new TimestampAction());

    rs.addRule(new Pattern("configuration/define"), new DefinePropertyAction());

    // the contextProperty pattern is deprecated. It is undocumented
    // and will be dropped in future versions of logback
    rs.addRule(new Pattern("configuration/contextProperty"),
        new ContextPropertyAction());

    rs.addRule(new Pattern("configuration/conversionRule"),
        new ConversionRuleAction());

    rs.addRule(new Pattern("configuration/statusListener"),
        new StatusListenerAction());

    rs.addRule(new Pattern("configuration/appender"), new AppenderAction());
    rs.addRule(new Pattern("configuration/appender/appender-ref"),
        new AppenderRefAction());
    rs.addRule(new Pattern("configuration/newRule"), new NewRuleAction());
    rs.addRule(new Pattern("configuration/componentFactory"), new ComponentFactoryAction());
    rs.addRule(new Pattern("*/param"), new ParamAction());
  }

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

  @Override
  protected void buildInterpreter() {
    super.buildInterpreter();
    Map<String, Object> omap = interpreter.getInterpretationContext()
        .getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
  }

  public InterpretationContext getExecutionContext() {
    return interpreter.getInterpretationContext();
  }
}
