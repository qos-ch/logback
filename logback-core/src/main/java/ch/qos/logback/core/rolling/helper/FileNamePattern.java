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
package ch.qos.logback.core.rolling.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.LiteralConverter;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.pattern.util.AlmostAsIsEscapeUtil;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ScanException;

/**
 * After parsing file name patterns, given a number or a date, instances of this
 * class can be used to compute a file name according to the file name pattern
 * and the current date or integer.
 *
 * @author Ceki G&uuml;lc&uuml;
 *
 */
public class FileNamePattern extends ContextAwareBase {

	static final Map<String, String> CONVERTER_MAP = new HashMap<>();
	static {
		CONVERTER_MAP.put(IntegerTokenConverter.CONVERTER_KEY, IntegerTokenConverter.class.getName());
		CONVERTER_MAP.put(DateTokenConverter.CONVERTER_KEY, DateTokenConverter.class.getName());
	}

	String pattern;
	Converter<Object> headTokenConverter;

	public FileNamePattern(final String patternArg, final Context contextArg) {
		// the pattern is slashified
		setPattern(FileFilterUtil.slashify(patternArg));
		setContext(contextArg);
		parse();
		ConverterUtil.startConverters(headTokenConverter);
	}


	void parse() {
		try {
			// http://jira.qos.ch/browse/LOGBACK-197
			// we escape ')' for parsing purposes. Note that the original pattern is preserved
			// because it is shown to the user in status messages. We don't want the escaped version
			// to leak out.
			final String patternForParsing = escapeRightParantesis(pattern);
			final Parser<Object> p = new Parser<>(patternForParsing, new AlmostAsIsEscapeUtil());
			p.setContext(context);
			final Node t = p.parse();
			headTokenConverter = p.compile(t, CONVERTER_MAP);

		} catch (final ScanException sce) {
			addError("Failed to parse pattern \"" + pattern + "\".", sce);
		}
	}

	String escapeRightParantesis(final String in) {
		return pattern.replace(")", "\\)");
	}

	@Override
	public String toString() {
		return pattern;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		final int result = 1;
		return prime * result + (pattern == null ? 0 : pattern.hashCode());
	}


	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final FileNamePattern other = (FileNamePattern) obj;
		if (!Objects.equals(pattern, other.pattern)) {
			return false;
		}
		return true;
	}


	public DateTokenConverter<Object> getPrimaryDateTokenConverter() {
		Converter<Object> p = headTokenConverter;

		while (p != null) {
			if (p instanceof DateTokenConverter) {
				final DateTokenConverter<Object> dtc = (DateTokenConverter<Object>) p;
				// only primary converters should be returned as
				if (dtc.isPrimary()) {
					return dtc;
				}
			}

			p = p.getNext();
		}

		return null;
	}

	public IntegerTokenConverter getIntegerTokenConverter() {
		Converter<Object> p = headTokenConverter;

		while (p != null) {
			if (p instanceof IntegerTokenConverter) {
				return (IntegerTokenConverter) p;
			}

			p = p.getNext();
		}
		return null;
	}

	public boolean hasIntegerTokenCOnverter() {
		final IntegerTokenConverter itc = getIntegerTokenConverter();
		return itc != null;
	}

	public String convertMultipleArguments(final Object... objectList) {
		final StringBuilder buf = new StringBuilder();
		Converter<Object> c = headTokenConverter;
		while (c != null) {
			if (c instanceof MonoTypedConverter) {
				final MonoTypedConverter monoTyped = (MonoTypedConverter) c;
				for (final Object o : objectList) {
					if (monoTyped.isApplicable(o)) {
						buf.append(c.convert(o));
					}
				}
			} else {
				buf.append(c.convert(objectList));
			}
			c = c.getNext();
		}
		return buf.toString();
	}

	public String convert(final Object o) {
		final StringBuilder buf = new StringBuilder();
		Converter<Object> p = headTokenConverter;
		while (p != null) {
			buf.append(p.convert(o));
			p = p.getNext();
		}
		return buf.toString();
	}

	public String convertInt(final int i) {
		return convert(i);
	}

	public void setPattern(final String pattern) {
		if (pattern != null) {
			// Trailing spaces in the pattern are assumed to be undesired.
			this.pattern = pattern.trim();
		}
	}

	public String getPattern() {
		return pattern;
	}


	/**
	 * Given date, convert this instance to a regular expression.
	 *
	 * Used to compute sub-regex when the pattern has both %d and %i, and the
	 * date is known.
	 *
	 * @param date - known date
	 */
	public String toRegexForFixedDate(final Date date) {
		final StringBuilder buf = new StringBuilder();
		Converter<Object> p = headTokenConverter;
		while (p != null) {
			if (p instanceof LiteralConverter) {
				buf.append(p.convert(null));
			} else if (p instanceof IntegerTokenConverter) {
				buf.append("(\\d+)");
			} else if (p instanceof DateTokenConverter) {
				buf.append(p.convert(date));
			}
			p = p.getNext();
		}
		return buf.toString();
	}

	/**
	 * Given date, convert this instance to a regular expression
	 */
	public String toRegex() {
		final StringBuilder buf = new StringBuilder();
		Converter<Object> p = headTokenConverter;
		while (p != null) {
			if (p instanceof LiteralConverter) {
				buf.append(p.convert(null));
			} else if (p instanceof IntegerTokenConverter) {
				buf.append("\\d+");
			} else if (p instanceof DateTokenConverter) {
				final DateTokenConverter<Object> dtc = (DateTokenConverter<Object>) p;
				buf.append(dtc.toRegex());
			}
			p = p.getNext();
		}
		return buf.toString();
	}
}
