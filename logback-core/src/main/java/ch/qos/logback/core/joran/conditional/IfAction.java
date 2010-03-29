package ch.qos.logback.core.joran.conditional;

import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.util.OptionHelper;

public class IfAction extends Action {
  private static final String CONDITION_ATTR = "condition";
  
  Boolean boolResult;
  List<SaxEvent> thenSaxEventList;
  List<SaxEvent> elseSaxEventList;

  @Override
  public void begin(InterpretationContext ic, String name, Attributes attributes)
      throws ActionException {

    ic.pushObject(this);
    Condition condition = null;
    String conditionAttribute = attributes.getValue(CONDITION_ATTR);
    if (!OptionHelper.isEmpty(conditionAttribute)) {
      PropertyEvalScriptBuilder pesb = new PropertyEvalScriptBuilder();
      pesb.setContext(context);
      try {
        condition = pesb.build(conditionAttribute);
      } catch (Exception e) {
        addError("Faield to parse condition ["+conditionAttribute+"]", e);
      }
     
      if(condition!=null) {
        boolResult = condition.evaluate();
      }
      
    }
  }


  @Override
  public void end(InterpretationContext ic, String name) throws ActionException {

    Object o = ic.peekObject();
    if (o == null) {
      throw new IllegalStateException("Unexpected null object on stack");
    }
    if (!(o instanceof IfAction)) {
      throw new IllegalStateException("Unexpected object of type ["
          + o.getClass() + "] on stack");
    }

    if (o != this) {
      throw new IllegalStateException(
          "IfAction different then current one on stack");
    }
    ic.popObject();

    if (boolResult == null) {
      addError("Failed to determine \"if then else\" result");
      return;
    }

    Interpreter interpreter = ic.getJoranInterpreter();
    List<SaxEvent> listToPlay = thenSaxEventList;
    if (!boolResult) {
      listToPlay = elseSaxEventList;
    }
    
    if (interpreter.pattern.peekLast().equals("if")) {
      interpreter.pattern.pop();
      interpreter.play(listToPlay);
      interpreter.pattern.push("if");
    }

  }

  public void setThenSaxEventList(List<SaxEvent> thenSaxEventList) {
    this.thenSaxEventList = thenSaxEventList;
  }

  public void setElseSaxEventList(List<SaxEvent> elseSaxEventList) {
    this.elseSaxEventList = elseSaxEventList;
  }

}
