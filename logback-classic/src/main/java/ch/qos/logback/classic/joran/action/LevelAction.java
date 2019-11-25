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

import ch.qos.logback.classic.model.LevelModel;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.action.PreconditionValidator;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

/**
 * Action to handle the <level> element nested within <logger> element. 
 * 
 * <p>This action is <b>deprecated</b>. Use the level attribute within the logger
 * element.
 * 
 * @author Ceki Gulcu
 */
public class LevelAction extends BaseModelAction {

	

	@Override
	protected boolean validPreconditions(InterpretationContext interpcont, String name,
			Attributes attributes) {
		PreconditionValidator pv = new PreconditionValidator(this, interpcont, name, attributes);
		pv.validateValueAttribute();
		addWarn("<level> element is deprecated. Near [" + name + "] on line " + Action.getLineNumber(interpcont));
		addWarn("Please use \"level\" attribute within <logger> or <root> elements instead.");
		return pv.isValid();
	}
	
	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
		LevelModel lm = new LevelModel();
		String value = attributes.getValue(JoranConstants.VALUE_ATTR);
		lm.setValue(value);
		
		return lm;
	}

	
}
