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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.Model;

public class AppenderRefAction extends BaseModelAction {

	@Override
	protected boolean validPreconditions(InterpretationContext intercon, String name,
			Attributes attributes) {
		PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
		pv.validateRefAttribute();
		return pv.isValid();
	}
	
	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
		AppenderRefModel arm = new AppenderRefModel();
        String ref = attributes.getValue(JoranConstants.REF_ATTRIBUTE);
		arm.setRef(ref);
		return arm;
	}

}
