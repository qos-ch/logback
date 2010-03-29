package ch.qos.logback.core.joran.conditional;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class ElseAction  extends Action implements InPlayListener {

  List<SaxEvent> saxEventList;
  
  @Override
  public void begin(InterpretationContext ic, String name, Attributes attributes)
      throws ActionException {
    saxEventList = new ArrayList<SaxEvent>();
    ic.addInPlayListener(this);
  }

  @Override
  public void end(InterpretationContext ic, String name) throws ActionException {
    ic.removeInPlayListener(this);
    Object o = ic.peekObject();
    if (o instanceof IfAction) {
      IfAction ifAction = (IfAction) o;
      removeFirstLastFromList();
      ifAction.setElseSaxEventList(saxEventList);
    }
  }

  public void inPlay(SaxEvent event) {
    saxEventList.add(event);
  }

  void removeFirstLastFromList() {
    saxEventList.remove(0);
    saxEventList.remove(saxEventList.size() - 1);
  }
}
