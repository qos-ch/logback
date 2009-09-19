/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.status.Status;

public class SaxEventRecorder extends DefaultHandler implements ContextAware {

  
  final ContextAwareImpl cai;
  
  public SaxEventRecorder() {
    cai =  new ContextAwareImpl(this);
  }
  public List<SaxEvent> saxEventList = new ArrayList<SaxEvent>();
  Locator locator;
  Pattern globalPattern = new Pattern();

 
  final public void recordEvents(InputStream inputStream) throws JoranException {
    recordEvents(new InputSource(inputStream));
  }

  public List<SaxEvent> recordEvents(InputSource inputSource)
      throws JoranException {
    SAXParser saxParser = null;
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      spf.setNamespaceAware(true);
      saxParser = spf.newSAXParser();
    } catch (Exception pce) {
      String errMsg = "Parser configuration error occured";
      addError(errMsg, pce);
      throw new JoranException(errMsg, pce);
    }

    try {
      saxParser.parse(inputSource, this);
      return saxEventList;

    } catch (IOException ie) {
      String errMsg = "I/O error occurred while parsing xml file";
      addError(errMsg, ie);
      throw new JoranException(errMsg, ie);
    } catch (Exception ex) {
      String errMsg = "Problem parsing XML document. See previously reported errors. Abandoning all further processing.";
      addError(errMsg, ex);
      throw new JoranException(errMsg, ex);
    }

  }

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
    saxEventList.add(new StartEvent(current, namespaceURI, localName, qName,
        atts, getLocator()));
  }

  public void characters(char[] ch, int start, int length) {

    String body = new String(ch, start, length);
    if (body == null) {
      return;
    }

    // if the body string is null
    if (body != null) {
      String bodyTrimmed = body.trim();
      if (bodyTrimmed.length() == 0) {
        return;
      }
    }

    SaxEvent lastEvent = getLastEvent();
    if (lastEvent instanceof BodyEvent) {
      BodyEvent be = (BodyEvent) lastEvent;
      be.append(body);
    } else {
      saxEventList.add(new BodyEvent(body, getLocator()));
    }

  }

  SaxEvent getLastEvent() {
    if (saxEventList.isEmpty()) {
      return null;
    }
    int size = saxEventList.size();
    return saxEventList.get(size - 1);
  }

  public void endElement(String namespaceURI, String localName, String qName) {
    saxEventList
        .add(new EndEvent(namespaceURI, localName, qName, getLocator()));
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
    addError("Parsing error on line " + spe.getLineNumber() + " and column "
        + spe.getColumnNumber(), spe);
  }

  public void fatalError(SAXParseException spe) throws SAXException {
    addError("Parsing fatal error on line " + spe.getLineNumber() + " and column "
        + spe.getColumnNumber(), spe);
  }

  public void warning(SAXParseException spe) throws SAXException {
    addWarn("Parsing warning on line " + spe.getLineNumber() + " and column "
        + spe.getColumnNumber(), spe);
  }

  public void addError(String msg) {
    cai.addError(msg);
  }

  public void addError(String msg, Throwable ex) {
    cai.addError(msg, ex);
  }

  public void addInfo(String msg) {
    cai.addInfo(msg);
  }

  public void addInfo(String msg, Throwable ex) {
    cai.addInfo(msg, ex);
  }

  public void addStatus(Status status) {
    cai.addStatus(status);
  }

  public void addWarn(String msg) {
    cai.addWarn(msg);
  }

  public void addWarn(String msg, Throwable ex) {
    cai.addWarn(msg, ex);
  }

  public Context getContext() {
    return cai.getContext();
  }

  public void setContext(Context context) {
    cai.setContext(context);
  }

  public List<SaxEvent> getSaxEventList() {
    return saxEventList;
  }

}
