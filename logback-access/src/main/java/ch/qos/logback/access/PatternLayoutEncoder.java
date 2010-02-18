package ch.qos.logback.access;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;


public class PatternLayoutEncoder extends PatternLayoutEncoderBase<AccessEvent> {

  @Override
  public void start() {
    PatternLayout patternLayout = new PatternLayout();
    patternLayout.setContext(context);
    patternLayout.setPattern(getPattern());
    patternLayout.start();
    this.layout = patternLayout;
    super.start();
  }
   
}