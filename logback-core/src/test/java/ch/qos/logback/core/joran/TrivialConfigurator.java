package ch.qos.logback.core.joran;

import java.util.HashMap;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class TrivialConfigurator extends GenericConfigurator {

  HashMap<Pattern, Action> rulesMap;
  
  TrivialConfigurator(HashMap<Pattern, Action> rules) {
    this.rulesMap = rules;
  }
  
  @Override
  protected void addImplicitRules(Interpreter interpreter) {
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    for(Pattern pattern : rulesMap.keySet()) {
      Action action = rulesMap.get(pattern);
      rs.addRule(pattern, action);
    }
  }

}
