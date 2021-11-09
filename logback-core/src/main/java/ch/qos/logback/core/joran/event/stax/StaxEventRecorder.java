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
import javax.xml.stream.XMLStreamConstants;
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

	List<StaxEvent> eventList = new ArrayList<>();
	ElementPath globalElementPath = new ElementPath();

	public StaxEventRecorder(final Context context) {
		setContext(context);
	}

	public void recordEvents(final InputStream inputStream) throws JoranException {
		try {
			final XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
			read(xmlEventReader);
		} catch (final XMLStreamException e) {
			throw new JoranException("Problem parsing XML document. See previously reported errors.", e);
		}
	}

	public List<StaxEvent> getEventList() {
		return eventList;
	}

	private void read(final XMLEventReader xmlEventReader) throws XMLStreamException {
		while (xmlEventReader.hasNext()) {
			final XMLEvent xmlEvent = xmlEventReader.nextEvent();
			switch (xmlEvent.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				addStartElement(xmlEvent);
				break;
			case XMLStreamConstants.CHARACTERS:
				addCharacters(xmlEvent);
				break;
			case XMLStreamConstants.END_ELEMENT:
				addEndEvent(xmlEvent);
				break;
			default:
				break;
			}
		}
	}

	private void addStartElement(final XMLEvent xmlEvent) {
		final StartElement se = xmlEvent.asStartElement();
		final String tagName = se.getName().getLocalPart();
		globalElementPath.push(tagName);
		final ElementPath current = globalElementPath.duplicate();
		final StartEvent startEvent = new StartEvent(current, tagName, se.getAttributes(), se.getLocation());
		eventList.add(startEvent);
	}

	private void addCharacters(final XMLEvent xmlEvent) {
		final Characters characters = xmlEvent.asCharacters();
		final StaxEvent lastEvent = getLastEvent();

		if (lastEvent instanceof BodyEvent) {
			final BodyEvent be = (BodyEvent) lastEvent;
			be.append(characters.getData());
		} else // ignore space only text if the previous event is not a BodyEvent
			if (!characters.isWhiteSpace()) {
				final BodyEvent bodyEvent = new BodyEvent(characters.getData(), xmlEvent.getLocation());
				eventList.add(bodyEvent);
			}
	}

	private void addEndEvent(final XMLEvent xmlEvent) {
		final EndElement ee = xmlEvent.asEndElement();
		final String tagName = ee.getName().getLocalPart();
		final EndEvent endEvent = new EndEvent(tagName, ee.getLocation());
		eventList.add(endEvent);
		globalElementPath.pop();
	}

	StaxEvent getLastEvent() {
		if (eventList.isEmpty()) {
			return null;
		}
		final int size = eventList.size();
		if (size == 0) {
			return null;
		}
		return eventList.get(size - 1);
	}

}
