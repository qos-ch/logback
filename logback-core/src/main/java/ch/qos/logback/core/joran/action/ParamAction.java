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

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ParamModel;

public class ParamAction extends BaseModelAction {

	@Override
	protected boolean validPreconditions(InterpretationContext intercon, String name,
			Attributes attributes) {
		PreconditionValidator pv = new PreconditionValidator(this, intercon, name, attributes);
		pv.validateNameAttribute();
		pv.validateValueAttribute();
		
		addWarn("<param> element is deprecated in favor of a more direct syntax." + atLine(intercon));
		addWarn("For details see " + CoreConstants.CODES_URL + "#param");
		
		return pv.isValid();
		
	}

	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
		ParamModel paramModel = new ParamModel();
		paramModel.setName(attributes.getValue(NAME_ATTRIBUTE));
		paramModel.setValue(attributes.getValue(VALUE_ATTRIBUTE));
		return paramModel;
	}
}
