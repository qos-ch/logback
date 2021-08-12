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
package ch.qos.logback.classic.helpers;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * An appender used for testing.
 * 
 * @author ceki
 * @param <E>
 * @since 1.3.0
 */
public class WithLayoutListAppender extends AppenderBase<ILoggingEvent> {

	public List<String> list = new ArrayList<>();

	String pattern;

	PatternLayout patternLayout;

	@Override
	public void start() {
		if(pattern == null) {
			addError("null pattern disallowed");
			return;
		}
		patternLayout = new PatternLayout();
		patternLayout.setContext(context);
		patternLayout.setPattern(pattern);
		patternLayout.start();
		if (patternLayout.isStarted())
			super.start();
	}

	protected void append(ILoggingEvent e) {
		String result = patternLayout.doLayout(e);
		list.add(result);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}
