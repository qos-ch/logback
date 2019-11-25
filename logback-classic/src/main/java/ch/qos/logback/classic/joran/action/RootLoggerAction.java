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
package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.model.RootLoggerModel;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class RootLoggerAction extends BaseModelAction {

    Logger root;
    boolean inError = false;

	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
		RootLoggerModel rootLoggerModel = new RootLoggerModel();
		String levelStr = attributes.getValue(JoranConstants.LEVEL_ATTRIBUTE);
		rootLoggerModel.setLevel(levelStr);
	        
		return rootLoggerModel;
	}

}
