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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;

import ch.qos.logback.core.joran.spi.ElementPath;

public class StartEvent extends StaxEvent {

	List<Attribute> attributes;
	public ElementPath elementPath;

	StartEvent(final ElementPath elementPath, final String name, final Iterator<Attribute> attributeIterator, final Location location) {
		super(name, location);
		populateAttributes(attributeIterator);
		this.elementPath = elementPath;
	}

	private void populateAttributes(final Iterator<Attribute> attributeIterator) {
		while (attributeIterator.hasNext()) {
			if (attributes == null) {
				attributes = new ArrayList<>(2);
			}
			attributes.add(attributeIterator.next());
		}
	}

	public ElementPath getElementPath() {
		return elementPath;
	}

	public List<Attribute> getAttributeList() {
		return attributes;
	}

	Attribute getAttributeByName(final String name) {
		if (attributes == null) {
			return null;
		}

		for (final Attribute attr : attributes) {
			if (name.equals(attr.getName().getLocalPart())) {
				return attr;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "StartEvent(" + getName() + ")  [" + location.getLineNumber() + "," + location.getColumnNumber() + "]";
	}
}
