/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import static ch.qos.logback.core.CoreConstants.XML_PARSING;

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
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareImpl;
import ch.qos.logback.core.status.Status;

public class SaxEventRecorder extends DefaultHandler implements ContextAware {

    final ContextAwareImpl contextAwareImpl;
    final ElementPath elementPath;
    public List<SaxEvent> saxEventList = new ArrayList<SaxEvent>();
    Locator locator;
    
    
    public SaxEventRecorder(Context context) {
    	this(context, new ElementPath());
    }

    public SaxEventRecorder(Context context, ElementPath elementPath) {
        contextAwareImpl = new ContextAwareImpl(context, this);
        this.elementPath = elementPath;
    }

    
    
    final public void recordEvents(InputStream inputStream) throws JoranException {
        recordEvents(new InputSource(inputStream));
    }

    public List<SaxEvent> recordEvents(InputSource inputSource) throws JoranException {
        SAXParser saxParser = buildSaxParser();
        try {
            saxParser.parse(inputSource, this);
            return saxEventList;
        } catch (IOException ie) {
            handleError("I/O error occurred while parsing xml file", ie);
        } catch (SAXException se) {
            // Exception added into StatusManager via Sax error handling. No need to add it again
            throw new JoranException("Problem parsing XML document. See previously reported errors.", se);
        } catch (Exception ex) {
            handleError("Unexpected exception while parsing XML document.", ex);
        }
        throw new IllegalStateException("This point can never be reached");
    }

    private void handleError(String errMsg, Throwable t) throws JoranException {
        addError(errMsg, t);
        throw new JoranException(errMsg, t);
    }

    private SAXParser buildSaxParser() throws JoranException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            //spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // See LOGBACK-1465
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            return spf.newSAXParser();
        } catch (Exception pce) {
            String errMsg = "Parser configuration error occurred";
            addError(errMsg, pce);
            throw new JoranException(errMsg, pce);
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

    protected boolean shouldIgnoreForElementPath(String tagName) {
    	return false;
    }
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {

        String tagName = getTagName(localName, qName);
        if(!shouldIgnoreForElementPath(tagName)) {
          elementPath.push(tagName);
        }
        ElementPath current = elementPath.duplicate();
        saxEventList.add(new StartEvent(current, namespaceURI, localName, qName, atts, getLocator()));
    }

    public void characters(char[] ch, int start, int length) {
        String bodyStr = new String(ch, start, length);
        SaxEvent lastEvent = getLastEvent();
        if (lastEvent instanceof BodyEvent) {
            BodyEvent be = (BodyEvent) lastEvent;
            be.append(bodyStr);
        } else {
            // ignore space only text if the previous event is not a BodyEvent
            if (!isSpaceOnly(bodyStr)) {
                saxEventList.add(new BodyEvent(bodyStr, getLocator()));
            }
        }
    }

    boolean isSpaceOnly(String bodyStr) {
        String bodyTrimmed = bodyStr.trim();
        return (bodyTrimmed.length() == 0);
    }

    SaxEvent getLastEvent() {
        if (saxEventList.isEmpty()) {
            return null;
        }
        int size = saxEventList.size();
        return saxEventList.get(size - 1);
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        saxEventList.add(new EndEvent(namespaceURI, localName, qName, getLocator()));
        String tagName = getTagName(localName, qName);
        if(!shouldIgnoreForElementPath(tagName)) {
          elementPath.pop();
        }
    }

    String getTagName(String localName, String qName) {
        String tagName = localName;
        if ((tagName == null) || (tagName.length() < 1)) {
            tagName = qName;
        }
        return tagName;
    }

    public void error(SAXParseException spe) throws SAXException {
        addError(XML_PARSING + " - Parsing error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        addError(spe.toString());
    }

    public void fatalError(SAXParseException spe) throws SAXException {
        addError(XML_PARSING + " - Parsing fatal error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        addError(spe.toString());
    }

    public void warning(SAXParseException spe) throws SAXException {
        addWarn(XML_PARSING + " - Parsing warning on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber(), spe);
    }

    public void addError(String msg) {
        contextAwareImpl.addError(msg);
    }

    public void addError(String msg, Throwable ex) {
        contextAwareImpl.addError(msg, ex);
    }

    public void addInfo(String msg) {
        contextAwareImpl.addInfo(msg);
    }

    public void addInfo(String msg, Throwable ex) {
        contextAwareImpl.addInfo(msg, ex);
    }

    public void addStatus(Status status) {
        contextAwareImpl.addStatus(status);
    }

    public void addWarn(String msg) {
        contextAwareImpl.addWarn(msg);
    }

    public void addWarn(String msg, Throwable ex) {
        contextAwareImpl.addWarn(msg, ex);
    }

    public Context getContext() {
        return contextAwareImpl.getContext();
    }

    public void setContext(Context context) {
        contextAwareImpl.setContext(context);
    }

    public List<SaxEvent> getSaxEventList() {
        return saxEventList;
    }

}
