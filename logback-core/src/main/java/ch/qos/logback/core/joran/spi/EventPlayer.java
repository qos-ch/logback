package ch.qos.logback.core.joran.spi;

import java.util.List;

public class EventPlayer {

  final Interpreter interpreter;
  
  public EventPlayer(Interpreter interpreter) {
    this.interpreter = interpreter;
  }
  
  public void play(List<SaxEvent> seList) {
    for(SaxEvent se : seList) {
      if(se instanceof StartEvent) {
        interpreter.startElement((StartEvent) se);
      }
      if(se instanceof BodyEvent) {
        interpreter.characters((BodyEvent) se);
      }
      if(se instanceof EndEvent) {
        interpreter.endElement((EndEvent) se);
      }
    }
  }
}
