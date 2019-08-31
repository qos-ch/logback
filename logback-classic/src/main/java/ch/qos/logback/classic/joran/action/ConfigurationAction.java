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

import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.joran.action.BaseModelAction;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.model.Model;

public class ConfigurationAction extends BaseModelAction {
	static final String INTERNAL_DEBUG_ATTR = "debug";
	static final String SCAN_ATTR = "scan";
	static final String SCAN_PERIOD_ATTR = "scanPeriod";
	static final String PACKAGING_DATA_ATTR = "packagingData";

	@Override
	protected Model buildCurrentModel(InterpretationContext interpretationContext, String name, Attributes attributes) {
		ConfigurationModel configurationModel = new ConfigurationModel();
		configurationModel.setDebugStr(attributes.getValue(INTERNAL_DEBUG_ATTR));
		configurationModel.setScanStr(attributes.getValue(SCAN_ATTR));
		configurationModel.setScanPeriodStr(attributes.getValue(SCAN_PERIOD_ATTR));
		configurationModel.setPackagingDataStr(attributes.getValue(PACKAGING_DATA_ATTR));
		return configurationModel;
	}


}
