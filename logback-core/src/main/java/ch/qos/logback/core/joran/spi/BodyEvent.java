package ch.qos.logback.core.joran.spi;

import org.xml.sax.Locator;

public class BodyEvent extends SaxEvent {
  
  final String text;
  
  BodyEvent(String text, Locator locator) {
    super(null, null, null, locator);
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  @Override
  public String toString() {
    return "BodyEvent("+getText()+")"+locator.getLineNumber()+","+locator.getColumnNumber();
  }

}
