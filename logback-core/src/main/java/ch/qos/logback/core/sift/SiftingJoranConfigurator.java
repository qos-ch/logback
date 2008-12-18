package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.joran.spi.RuleStore;

public abstract class SiftingJoranConfigurator<E> extends GenericConfigurator {

  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void addInstanceRules(RuleStore rs) {
    // TODO Auto-generated method stub
    
  }

  abstract Appender<E> getAppender();
}
