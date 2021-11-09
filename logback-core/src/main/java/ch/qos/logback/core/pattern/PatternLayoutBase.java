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

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;

abstract public class PatternLayoutBase<E> extends LayoutBase<E> {

	static final int INTIAL_STRING_BUILDER_SIZE = 256;
	Converter<E> head;
	String pattern;
	protected PostCompileProcessor<E> postCompileProcessor;

	Map<String, String> instanceConverterMap = new HashMap<>();
	protected boolean outputPatternAsHeader = false;

	/**
	 * Concrete implementations of this class are responsible for elaborating the
	 * mapping between pattern words and converters.
	 *
	 * @return A map associating pattern words to the names of converter classes
	 */
	abstract public Map<String, String> getDefaultConverterMap();

	/**
	 * Returns a map where the default converter map is merged with the map
	 * contained in the context.
	 */
	public Map<String, String> getEffectiveConverterMap() {
		final Map<String, String> effectiveMap = new HashMap<>();

		// add the least specific map fist
		final Map<String, String> defaultMap = getDefaultConverterMap();
		if (defaultMap != null) {
			effectiveMap.putAll(defaultMap);
		}

		// contextMap is more specific than the default map
		final Context context = getContext();
		if (context != null) {
			@SuppressWarnings("unchecked")
			final
			Map<String, String> contextMap = (Map<String, String>) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
			if (contextMap != null) {
				effectiveMap.putAll(contextMap);
			}
		}
		// set the most specific map last
		effectiveMap.putAll(instanceConverterMap);
		return effectiveMap;
	}

	@Override
	public void start() {
		if (pattern == null || pattern.length() == 0) {
			addError("Empty or null pattern.");
			return;
		}
		try {
			final Parser<E> p = new Parser<>(pattern);
			if (getContext() != null) {
				p.setContext(getContext());
			}
			final Node t = p.parse();
			this.head = p.compile(t, getEffectiveConverterMap());
			if (postCompileProcessor != null) {
				postCompileProcessor.process(context, head);
			}
			ConverterUtil.setContextForConverters(getContext(), head);
			ConverterUtil.startConverters(this.head);
			super.start();
		} catch (final ScanException sce) {
			final StatusManager sm = getContext().getStatusManager();
			sm.add(new ErrorStatus("Failed to parse pattern \"" + getPattern() + "\".", this, sce));
		}
	}

	public void setPostCompileProcessor(final PostCompileProcessor<E> postCompileProcessor) {
		this.postCompileProcessor = postCompileProcessor;
	}

	/**
	 *
	 * @param head
	 * @deprecated  Use {@link ConverterUtil#setContextForConverters} instead. This method will
	 *  be removed in future releases.
	 */
	@Deprecated
	protected void setContextForConverters(final Converter<E> head) {
		ConverterUtil.setContextForConverters(getContext(), head);
	}

	protected String writeLoopOnConverters(final E event) {
		final StringBuilder strBuilder = new StringBuilder(INTIAL_STRING_BUILDER_SIZE);
		Converter<E> c = head;
		while (c != null) {
			c.write(strBuilder, event);
			c = c.getNext();
		}
		return strBuilder.toString();
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "(\"" + getPattern() + "\")";
	}

	public Map<String, String> getInstanceConverterMap() {
		return instanceConverterMap;
	}

	protected String getPresentationHeaderPrefix() {
		return CoreConstants.EMPTY_STRING;
	}

	public boolean isOutputPatternAsHeader() {
		return outputPatternAsHeader;
	}

	public void setOutputPatternAsHeader(final boolean outputPatternAsHeader) {
		this.outputPatternAsHeader = outputPatternAsHeader;
	}

	@Override
	public String getPresentationHeader() {
		if (outputPatternAsHeader) {
			return getPresentationHeaderPrefix() + pattern;
		}
		return super.getPresentationHeader();
	}
}
