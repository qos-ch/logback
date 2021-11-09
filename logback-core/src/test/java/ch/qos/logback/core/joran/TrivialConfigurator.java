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
package ch.qos.logback.core.joran;

import java.util.HashMap;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.joran.spi.SaxEventInterpreter;

public class TrivialConfigurator extends GenericConfigurator {

	HashMap<ElementSelector, Action> rulesMap;

	public TrivialConfigurator(final HashMap<ElementSelector, Action> rules) {
		rulesMap = rules;
	}

	@Override
	protected void addImplicitRules(final SaxEventInterpreter interpreter) {
	}

	@Override
	protected void addInstanceRules(final RuleStore rs) {
		for (final ElementSelector elementSelector : rulesMap.keySet()) {
			final Action action = rulesMap.get(elementSelector);
			rs.addRule(elementSelector, action);
		}
	}

}
