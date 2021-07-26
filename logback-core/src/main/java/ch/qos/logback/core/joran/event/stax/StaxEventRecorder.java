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
package ch.qos.logback.core.joran.event.stax;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;

public class StaxEventRecorder extends ContextAwareBase {

    List<StaxEvent> eventList = new ArrayList<StaxEvent>();
    ElementPath globalElementPath = new ElementPath();

    public StaxEventRecorder(Context context) {
        setContext(context);
    }

    public void recordEvents(InputStream inputStream) throws JoranException {
        try {
            XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
            read(xmlEventReader);
        } catch (XMLStreamException e) {
            throw new JoranException("Problem parsing XML document. See previously reported errors.", e);
        }
    }

    public List<StaxEvent> getEventList() {
        return eventList;
    }

    private void read(XMLEventReader xmlEventReader) throws XMLStreamException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            switch (xmlEvent.getEventType()) {
            case XMLEvent.START_ELEMENT:
                addStartElement(xmlEvent);
                break;
            case XMLEvent.CHARACTERS:
                addCharacters(xmlEvent);
                break;
            case XMLEvent.END_ELEMENT:
                addEndEvent(xmlEvent);
                break;
            default:
                break;
            }
        }
    }

    private void addStartElement(XMLEvent xmlEvent) {
        StartElement se = xmlEvent.asStartElement();
        String tagName = se.getName().getLocalPart();
        this.globalElementPath.push(tagName);
        ElementPath current = globalElementPath.duplicate();
		StartEvent startEvent = new StartEvent(current, tagName, se.getAttributes(), se.getLocation());
        eventList.add(startEvent);
    }

    private void addCharacters(XMLEvent xmlEvent) {
        Characters characters = xmlEvent.asCharacters();
        StaxEvent lastEvent = getLastEvent();

        if (lastEvent instanceof BodyEvent) {
            BodyEvent be = (BodyEvent) lastEvent;
            be.append(characters.getData());
        } else {
            // ignore space only text if the previous event is not a BodyEvent
            if (!characters.isWhiteSpace()) {
                BodyEvent bodyEvent = new BodyEvent(characters.getData(), xmlEvent.getLocation());
                eventList.add(bodyEvent);
            }
        }
    }

    private void addEndEvent(XMLEvent xmlEvent) {
        EndElement ee = xmlEvent.asEndElement();
        String tagName = ee.getName().getLocalPart();
        EndEvent endEvent = new EndEvent(tagName, ee.getLocation());
        eventList.add(endEvent);
        this.globalElementPath.pop();
    }

    StaxEvent getLastEvent() {
        if (eventList.isEmpty()) {
            return null;
        }
        int size = eventList.size();
        if (size == 0)
            return null;
        return eventList.get(size - 1);
    }

}
