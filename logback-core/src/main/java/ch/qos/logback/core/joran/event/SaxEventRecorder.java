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
    public List<SaxEvent> saxEventList = new ArrayList<>();
    Locator locator;

    public SaxEventRecorder(final Context context) {
        this(context, new ElementPath());
    }

    public SaxEventRecorder(final Context context, final ElementPath elementPath) {
        contextAwareImpl = new ContextAwareImpl(context, this);
        this.elementPath = elementPath;
    }

    final public void recordEvents(final InputStream inputStream) throws JoranException {
        recordEvents(new InputSource(inputStream));
    }

    public List<SaxEvent> recordEvents(final InputSource inputSource) throws JoranException {
        final SAXParser saxParser = buildSaxParser();
        try {
            saxParser.parse(inputSource, this);
            return saxEventList;
        } catch (final IOException ie) {
            handleError("I/O error occurred while parsing xml file", ie);
        } catch (final SAXException se) {
            // Exception added into StatusManager via Sax error handling. No need to add it again
            throw new JoranException("Problem parsing XML document. See previously reported errors.", se);
        } catch (final Exception ex) {
            handleError("Unexpected exception while parsing XML document.", ex);
        }
        throw new IllegalStateException("This point can never be reached");
    }

    private void handleError(final String errMsg, final Throwable t) throws JoranException {
        addError(errMsg, t);
        throw new JoranException(errMsg, t);
    }

    private SAXParser buildSaxParser() throws JoranException {
        try {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            // spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // See LOGBACK-1465
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setNamespaceAware(true);
            return spf.newSAXParser();
        } catch (final Exception pce) {
            final String errMsg = "Parser configuration error occurred";
            addError(errMsg, pce);
            throw new JoranException(errMsg, pce);
        }
    }

    @Override
    public void startDocument() {
    }

    public Locator getLocator() {
        return locator;
    }

    @Override
    public void setDocumentLocator(final Locator l) {
        locator = l;
    }

    protected boolean shouldIgnoreForElementPath(final String tagName) {
        return false;
    }

    @Override
    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts) {

        final String tagName = getTagName(localName, qName);
        if (!shouldIgnoreForElementPath(tagName)) {
            elementPath.push(tagName);
        }
        final ElementPath current = elementPath.duplicate();
        saxEventList.add(new StartEvent(current, namespaceURI, localName, qName, atts, getLocator()));
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        final String bodyStr = new String(ch, start, length);
        final SaxEvent lastEvent = getLastEvent();
        if (lastEvent instanceof BodyEvent) {
            final BodyEvent be = (BodyEvent) lastEvent;
            be.append(bodyStr);
        } else // ignore space only text if the previous event is not a BodyEvent
        if (!isSpaceOnly(bodyStr)) {
            saxEventList.add(new BodyEvent(bodyStr, getLocator()));
        }
    }

    boolean isSpaceOnly(final String bodyStr) {
        final String bodyTrimmed = bodyStr.trim();
        return bodyTrimmed.length() == 0;
    }

    SaxEvent getLastEvent() {
        if (saxEventList.isEmpty()) {
            return null;
        }
        final int size = saxEventList.size();
        return saxEventList.get(size - 1);
    }

    @Override
    public void endElement(final String namespaceURI, final String localName, final String qName) {
        saxEventList.add(new EndEvent(namespaceURI, localName, qName, getLocator()));
        final String tagName = getTagName(localName, qName);
        if (!shouldIgnoreForElementPath(tagName)) {
            elementPath.pop();
        }
    }

    String getTagName(final String localName, final String qName) {
        String tagName = localName;
        if (tagName == null || tagName.length() < 1) {
            tagName = qName;
        }
        return tagName;
    }

    @Override
    public void error(final SAXParseException spe) throws SAXException {
        addError(XML_PARSING + " - Parsing error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        addError(spe.toString());
    }

    @Override
    public void fatalError(final SAXParseException spe) throws SAXException {
        addError(XML_PARSING + " - Parsing fatal error on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber());
        addError(spe.toString());
    }

    @Override
    public void warning(final SAXParseException spe) throws SAXException {
        addWarn(XML_PARSING + " - Parsing warning on line " + spe.getLineNumber() + " and column " + spe.getColumnNumber(), spe);
    }

    @Override
    public void addError(final String msg) {
        contextAwareImpl.addError(msg);
    }

    @Override
    public void addError(final String msg, final Throwable ex) {
        contextAwareImpl.addError(msg, ex);
    }

    @Override
    public void addInfo(final String msg) {
        contextAwareImpl.addInfo(msg);
    }

    @Override
    public void addInfo(final String msg, final Throwable ex) {
        contextAwareImpl.addInfo(msg, ex);
    }

    @Override
    public void addStatus(final Status status) {
        contextAwareImpl.addStatus(status);
    }

    @Override
    public void addWarn(final String msg) {
        contextAwareImpl.addWarn(msg);
    }

    @Override
    public void addWarn(final String msg, final Throwable ex) {
        contextAwareImpl.addWarn(msg, ex);
    }

    @Override
    public Context getContext() {
        return contextAwareImpl.getContext();
    }

    @Override
    public void setContext(final Context context) {
        contextAwareImpl.setContext(context);
    }

    public List<SaxEvent> getSaxEventList() {
        return saxEventList;
    }

}
