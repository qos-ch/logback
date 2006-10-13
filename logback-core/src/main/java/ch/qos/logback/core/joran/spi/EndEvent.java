package ch.qos.logback.core.joran.spi;

import org.xml.sax.Locator;

public class EndEvent extends SaxEvent {

  EndEvent(String namespaceURI, String localName, String qName, Locator locator) {
    super(namespaceURI, localName, qName, locator);
  }

  @Override
  public String toString() {
    return "  EndEvent("+getQName()+")  ["+locator.getLineNumber()+","+locator.getColumnNumber()+"]";
  }


}
