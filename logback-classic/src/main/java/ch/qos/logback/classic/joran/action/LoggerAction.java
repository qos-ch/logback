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

import ch.qos.logback.classic.model.LoggerModel;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

/**
 * Action which handles <logger> elements in configuration files.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class LoggerAction extends BaseModelAction {

    @Override
    protected boolean validPreconditions(InterpretationContext ic, String name, Attributes attributes) {
    	PreconditionValidator validator = new PreconditionValidator(this, ic, name, attributes);
    	validator.validateNameAttribute();
    	return validator.isValid();
    }

	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {

		LoggerModel loggerModel = new LoggerModel();
		
		
		String nameStr = attributes.getValue(NAME_ATTRIBUTE);
		loggerModel.setName(nameStr);
		
		String levelStr = attributes.getValue(JoranConstants.LEVEL_ATTRIBUTE);
		loggerModel.setLevel(levelStr);
	    
		String additivityStr = attributes.getValue(JoranConstants.ADDITIVITY_ATTRIBUTE);
		loggerModel.setAdditivity(additivityStr);
	    
		return loggerModel;
	}
}
