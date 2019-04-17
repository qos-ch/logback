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
package ch.qos.logback.core.joran.action;

import static ch.qos.logback.core.joran.JoranConstants.INCLUDE_TAG;

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.spi.ElementPath;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.IncludeModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.util.OptionHelper;

public class IncludeModelAction extends BaseModelAction {

	private static final String FILE_ATTR = "file";
	private static final String URL_ATTR = "url";
	private static final String RESOURCE_ATTR = "resource";
	private static final String OPTIONAL_ATTR = "optional";

	@Override
	protected Model buildCurrentModel(InterpretationContext intercon, String name, Attributes attributes) {
		IncludeModel includeModel = new IncludeModel();
		
		
		includeModel.setOptional(attributes.getValue(OPTIONAL_ATTR));
		includeModel.setFile(attributes.getValue(FILE_ATTR));
		includeModel.setUrl(attributes.getValue(URL_ATTR));
		includeModel.setResource(attributes.getValue(RESOURCE_ATTR));
		ElementPath elementPath = intercon.getSaxEventInterpreter().getCopyOfElementPath();
		
		String lastPath = elementPath.peekLast();
		
		if(!INCLUDE_TAG.equalsIgnoreCase(lastPath)) {
			addWarn("expecting [include] but got ["+lastPath+"]");
		}

		// remove [include] part
		elementPath.pop();
		includeModel.setElementPath(elementPath);
		return includeModel;
	}

	@Override
	protected boolean validPreconditions(InterpretationContext intercon, String name, Attributes attributes) {
		String fileAttribute = attributes.getValue(FILE_ATTR);
		String urlAttribute = attributes.getValue(URL_ATTR);
		String resourceAttribute = attributes.getValue(RESOURCE_ATTR);

		int count = 0;

		if (!OptionHelper.isNullOrEmpty(fileAttribute)) {
			count++;
		}
		if (!OptionHelper.isNullOrEmpty(urlAttribute)) {
			count++;
		}
		if (!OptionHelper.isNullOrEmpty(resourceAttribute)) {
			count++;
		}

		String attributeNames = "\"" + FILE_ATTR + "\", \"" + RESOURCE_ATTR + "\" or \"" + URL_ATTR + "\"";
		if (count == 1) {
			return true;
		} else {
			addError("One and only one of the attributes " + attributeNames + " should be set. " + nearLine(intercon));
			return false;
		}
	}
}
