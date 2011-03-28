package ch.qos.logback.access;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;


public class PatternLayoutEncoder extends PatternLayoutEncoderBase<IAccessEvent> {

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
