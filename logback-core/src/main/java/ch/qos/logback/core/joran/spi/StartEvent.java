package ch.qos.logback.core.joran.spi;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class StartEvent extends SaxEvent {

  final Attributes attributes;
  final Pattern pattern;
  
  StartEvent(Pattern pattern, String namespaceURI, String localName, String qName,
      Attributes attributes, Locator locator) {
    super(namespaceURI, localName, qName, locator);
    // locator impl is used to take a snapshot!
    this.attributes = attributes;
    this.pattern = pattern;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  
  @Override
  public String toString() {
    return "StartEvent("+getQName()+")  ["+locator.getLineNumber()+","+locator.getColumnNumber()+"]";
  }

}
