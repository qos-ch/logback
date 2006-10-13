package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxEventRecorder extends DefaultHandler {
  
  private ExecutionContext ec;
  
  public List<SaxEvent> saxEventList = new ArrayList<SaxEvent>();
  Locator locator;
  Pattern globalPattern;
  
  public void startDocument() {
  }

  public Locator getLocator() {
    return locator;
  }

  public void setDocumentLocator(Locator l) {
    locator = l;
  }

  
  public void startElement(String namespaceURI, String localName, String qName,
      Attributes atts) {

    String tagName = getTagName(localName, qName);
    globalPattern.push(tagName);
    Pattern current = (Pattern) globalPattern.clone();
    saxEventList.add(new StartEvent(current, namespaceURI, localName, qName, atts, getLocator()));
   }

  public void characters(char[] ch, int start, int length) {
    String body = new String(ch, start, length);    

    if(body != null) {
      body = body.trim();
    }
    if(body.length() > 0) {
      saxEventList.add(new BodyEvent(body, getLocator()));
    }
  }
  
  public void endElement(String namespaceURI, String localName, String qName) {
    saxEventList.add(new EndEvent(namespaceURI, localName, qName, getLocator()));
    globalPattern.pop();
  }

  
  String getTagName(String localName, String qName) {
    String tagName = localName;
    if ((tagName == null) || (tagName.length() < 1)) {
      tagName = qName;
    }
    return tagName;
  }
  
  public void error(SAXParseException spe) throws SAXException {
    ec.addError("Parsing error", this, spe);
    ec.addError("Parsing problem on line " + spe.getLineNumber()
        + " and column " + spe.getColumnNumber(), this, spe);
  }

  public void fatalError(SAXParseException spe) throws SAXException {
    ec.addError("Parsing fatal error", this, spe);
    ec.addError("Parsing problem on line " + spe.getLineNumber()
        + " and column " + spe.getColumnNumber(), this, spe);
  }

  public void warning(SAXParseException spe) throws SAXException {
    ec.addWarn("Parsing warning", this, spe);
    ec.addWarn("Parsing problem on line " + spe.getLineNumber()
        + " and column " + spe.getColumnNumber(), this, spe);
  }
}
