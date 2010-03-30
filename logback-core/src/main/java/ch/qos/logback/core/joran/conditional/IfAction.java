package ch.qos.logback.core.joran.conditional;

import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Interpreter;
import ch.qos.logback.core.util.OptionHelper;

public class IfAction extends Action {
  private static final String CONDITION_ATTR = "condition";
  

  Stack<IfState> stack = new Stack<IfState>();
  
  @Override
  public void begin(InterpretationContext ic, String name, Attributes attributes)
      throws ActionException {

    IfState state = new IfState();
    boolean emptyStack = stack.isEmpty();
    stack.push(state);

    if(!emptyStack) {
      return;
    }
    
    state.active = true;
    ic.pushObject(this);
    
    Condition condition = null;
    String conditionAttribute = attributes.getValue(CONDITION_ATTR);
    if (!OptionHelper.isEmpty(conditionAttribute)) {
      conditionAttribute = OptionHelper.substVars(conditionAttribute, context);
      PropertyEvalScriptBuilder pesb = new PropertyEvalScriptBuilder();
      pesb.setContext(context);
      try {
        condition = pesb.build(conditionAttribute);
      } catch (Exception e) {
        addError("Faield to parse condition ["+conditionAttribute+"]", e);
      }
     
      if(condition!=null) {
        state.boolResult = condition.evaluate();
      }
      
    }
  }


  @Override
  public void end(InterpretationContext ic, String name) throws ActionException {

    IfState state = stack.pop();
    if(!state.active) {
      return;
    }
   
    
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

    if (state.boolResult == null) {
      addError("Failed to determine \"if then else\" result");
      return;
    }

    Interpreter interpreter = ic.getJoranInterpreter();
    List<SaxEvent> listToPlay = state.thenSaxEventList;
    if (!state.boolResult) {
      listToPlay = state.elseSaxEventList;
    }
    
    // insert past this event
    interpreter.addEventsDynamically(listToPlay, 1);

  }


  public void setThenSaxEventList(List<SaxEvent> thenSaxEventList) {
    IfState state = stack.firstElement();
    if(state.active) {
      state.thenSaxEventList = thenSaxEventList;
    } else {
      throw new IllegalStateException("setThenSaxEventList() invoked on inactive IfAction");
    }
  }

  public void setElseSaxEventList(List<SaxEvent> elseSaxEventList) {
    IfState state = stack.firstElement();
    if(state.active) {
      state.elseSaxEventList = elseSaxEventList;
    } else {
      throw new IllegalStateException("setElseSaxEventList() invoked on inactive IfAction");
    }

  }

}

class IfState {
  Boolean boolResult;
  List<SaxEvent> thenSaxEventList;
  List<SaxEvent> elseSaxEventList;
  boolean active;
}
