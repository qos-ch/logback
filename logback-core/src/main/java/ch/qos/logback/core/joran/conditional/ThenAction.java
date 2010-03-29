package ch.qos.logback.core.joran.conditional;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class ThenAction  extends Action implements InPlayListener {

  List<SaxEvent> eventList;
  
  @Override
  public void begin(InterpretationContext ic, String name, Attributes attributes)
      throws ActionException {
    eventList = new ArrayList<SaxEvent>();
    ic.addInPlayListener(this);
  }

  @Override
  public void end(InterpretationContext ic, String name) throws ActionException {
    ic.removeInPlayListener(this);
    Object o = ic.peekObject();
    if (o instanceof IfAction) {
      IfAction ifAction = (IfAction) o;
      removeFirstLastFromList();
      ifAction.setThenSaxEventList(eventList);
    } else {
      throw new IllegalStateException("Missing IfAction on top of stack");
    }
  }

  public void inPlay(SaxEvent event) {
    eventList.add(event);
  }

  void removeFirstLastFromList() {
    eventList.remove(0);
    eventList.remove(eventList.size() - 1);
  }
}
