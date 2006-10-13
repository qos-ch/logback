package ch.qos.logback.core.joran.spi;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class SaxEvent {

  final String namespaceURI;
  final String localName;
  final String qName;
  final Locator locator;

  SaxEvent(String namespaceURI, String localName, String qName, Locator locator) {
    this.namespaceURI = namespaceURI;
    this.localName = localName;
    this.qName = qName;
    // locator impl is used to take a snapshot!
    this.locator = new LocatorImpl(locator);
  }

  public String getLocalName() {
    return localName;
  }

  public Locator getLocator() {
    return locator;
  }

  public String getNamespaceURI() {
    return namespaceURI;
  }

  public String getQName() {
    return qName;
  }

  @Override
  public String toString() {
    return this.getClass().getName()+", "+locator.getLineNumber()+","+locator.getColumnNumber();
  }
}
