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
package ch.qos.logback.core.pattern.parser;

import java.util.List;

public class SimpleKeywordNode extends FormattingNode {

	List<String> optionList;

	SimpleKeywordNode(final Object value) {
		super(Node.SIMPLE_KEYWORD, value);
	}

	protected SimpleKeywordNode(final int type, final Object value) {
		super(type, value);
	}

	public List<String> getOptions() {
		return optionList;
	}

	public void setOptions(final List<String> optionList) {
		this.optionList = optionList;
	}

	@Override
	public boolean equals(final Object o) {
		if (!super.equals(o) || !(o instanceof SimpleKeywordNode)) {
			return false;
		}
		final SimpleKeywordNode r = (SimpleKeywordNode) o;

		return optionList != null ? optionList.equals(r.optionList) : r.optionList == null;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		if (optionList == null) {
			buf.append("KeyWord(" + value + "," + formatInfo + ")");
		} else {
			buf.append("KeyWord(" + value + ", " + formatInfo + "," + optionList + ")");
		}
		buf.append(printNext());
		return buf.toString();
	}
}
