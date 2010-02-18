package ch.qos.logback.access;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.pattern.PatternEncoderBase;


public class PatternEncoder extends PatternEncoderBase<AccessEvent> {

  public void start() {
    layout = new PatternLayout();
    layout.setContext(context);
    layout.setPattern(getPattern());
    layout.start();
  }
   
}