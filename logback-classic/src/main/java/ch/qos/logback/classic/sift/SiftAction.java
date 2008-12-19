package ch.qos.logback.classic.sift;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;

public class SiftAction  extends Action implements InPlayListener {
  List<SaxEvent> seList;
  
  @Override
  public void begin(InterpretationContext ec, String name, Attributes attributes)
      throws ActionException {
    seList = new ArrayList<SaxEvent>();
    ec.addInPlayListener(this);
  }

  @Override
  public void end(InterpretationContext ec, String name) throws ActionException {
    ec.removeInPlayListener(this);
    Object o = ec.peekObject();
    if (o instanceof SiftingAppender) {
      SiftingAppender ha = (SiftingAppender) o; 
      AppenderFactory appenderFactory = new AppenderFactory(context, seList, ha.getMdcKey());
      ha.setAppenderFactory(appenderFactory);
    }
  }

  public void inPlay(SaxEvent event) {
    seList.add(event);
    System.out.println(event);
  }

  public List<SaxEvent> getSeList() {
    return seList;
  }

    


}
