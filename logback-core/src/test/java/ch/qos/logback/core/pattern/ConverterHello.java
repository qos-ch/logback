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
package ch.qos.logback.core.pattern;

import java.util.List;

public class ConverterHello extends DynamicConverter<Object> {

	
	String firstOption;
	
	@Override
	public void start() {
		List<String> options = getOptionList();
		if(options != null && !options.isEmpty())
			firstOption = options.get(0);
		super.start();
	}
	
    public String convert(Object event) {
        return "Hello";
    }

}
