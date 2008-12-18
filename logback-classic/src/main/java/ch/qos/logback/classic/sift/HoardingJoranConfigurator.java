package ch.qos.logback.classic.sift;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.action.AppenderAction;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class HoardingJoranConfigurator  extends GenericConfigurator {

  String key;
  String value;
  
  HoardingJoranConfigurator(String key, String value) {
    this.key = key;
    this.value = value;
  }
  @Override
  protected Pattern initialPattern() {
    return new Pattern("configuration");
  }
  
  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    NestedComplexPropertyIA nestedComplexIA = new NestedComplexPropertyIA();
    nestedComplexIA.setContext(context);
    interpreter.addImplicitAction(nestedComplexIA);
    
    NestedBasicPropertyIA nestedSimpleIA = new NestedBasicPropertyIA();
    nestedSimpleIA.setContext(context);
    interpreter.addImplicitAction(nestedSimpleIA);
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("configuration/appender"), new AppenderAction());
  }

  @Override
  protected void buildInterpreter() {
    super.buildInterpreter();
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    omap.put(ActionConst.APPENDER_BAG, new HashMap());
    omap.put(ActionConst.FILTER_CHAIN_BAG, new HashMap());
    Map<String, String> propertiesMap = new HashMap<String, String>();
    propertiesMap.put(key, value);
    interpreter.setInterpretationContextPropertiesMap(propertiesMap);
  }

  @SuppressWarnings("unchecked")
  public Appender<LoggingEvent> getAppender() {
    Map<String, Object> omap = interpreter.getInterpretationContext().getObjectMap();
    HashMap map = (HashMap) omap.get(ActionConst.APPENDER_BAG);
    Collection values = map.values();
    return (Appender<LoggingEvent>) values.iterator().next();
  }
}
