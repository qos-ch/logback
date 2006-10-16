package ch.qos.logback.core.joran;

import ch.qos.logback.core.joran.action.IncAction;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;

public class TrivialConfigurator extends GenericConfigurator {

  @Override
  protected void addImplicitRules(Interpreter interpreter) {
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    rs.addRule(new Pattern("x/inc"), new IncAction());

  }

}
