package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.GenericConfigurator;
import ch.qos.logback.core.joran.action.NestedBasicPropertyIA;
import ch.qos.logback.core.joran.action.NestedComplexPropertyIA;
import ch.qos.logback.core.joran.spi.Interpreter;

public abstract class SiftingJoranConfiguratorBase<E> extends GenericConfigurator {

  @Override
  protected void addImplicitRules(Interpreter interpreter) {
    NestedComplexPropertyIA nestedComplexIA = new NestedComplexPropertyIA();
    nestedComplexIA.setContext(context);
    interpreter.addImplicitAction(nestedComplexIA);
    
    NestedBasicPropertyIA nestedSimpleIA = new NestedBasicPropertyIA();
    nestedSimpleIA.setContext(context);
    interpreter.addImplicitAction(nestedSimpleIA);
  }
  
  abstract public Appender<E> getAppender();
}
