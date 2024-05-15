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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    // instance converters whose value is a class name
    Map<String, String> instanceConverterMap = new HashMap<String, String>();
    // instance converters whose value is a supplier function
    Map<String, Supplier<Converter<E>>> instanceConverterSupplierMap = new HashMap<String, Supplier<Converter<E>>>();
    protected boolean outputPatternAsHeader = false;

    /**
     * Concrete implementations of this class are responsible for elaborating the
     * mapping between pattern words and converters.
     * 
     * @return A map associating pattern words to the names of converter classes
     */
    abstract public Map<String, String> getDefaultConverterMap();

    /**
     * Concrete implementations of this class may override this for elaborating the
     * mapping between pattern words and converters.
     * 
     * @return A map associating pattern words to the supplier of converter instances
     */
    public Map<String, Supplier<Converter<E>>> getDefaultConverterSupplierMap() {
        return Collections.emptyMap();
    }

    /**
     * Returns a map where the default converter map is merged with the map
     * contained in the context.
     */
    public Map<String, String> getEffectiveConverterMap() {
        Map<String, String> effectiveMap = new HashMap<String, String>();

        // add the least specific map fist
        Map<String, String> defaultMap = getDefaultConverterMap();
        if (defaultMap != null) {
            effectiveMap.putAll(defaultMap);
        }

        // contextMap is more specific than the default map
        Context context = getContext();
        if (context != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> contextMap = (Map<String, String>) context
                    .getObject(CoreConstants.PATTERN_RULE_REGISTRY);
            if (contextMap != null) {
                effectiveMap.putAll(contextMap);
            }
        }
        // set the most specific map last
        effectiveMap.putAll(instanceConverterMap);
        return effectiveMap;
    }

    /**
     * Returns a map where the default converter supplier map is merged with the
     * instance converter supplier map
     */
    public Map<String, Supplier<Converter<E>>> getEffectiveConverterSupplierMap() {
        Map<String, Supplier<Converter<E>>> effectiveMap = new HashMap<String, Supplier<Converter<E>>>();

        // add the least specific map fist
        Map<String, Supplier<Converter<E>>> defaultMap = getDefaultConverterSupplierMap();
        if (defaultMap != null) {
            effectiveMap.putAll(defaultMap);
        }

        // set the most specific map last
        effectiveMap.putAll(getInstanceConverterSupplierMap());
        return effectiveMap;
    }

    public void start() {
        if (pattern == null || pattern.length() == 0) {
            addError("Empty or null pattern.");
            return;
        }
        try {
            Parser<E> p = new Parser<E>(pattern);
            if (getContext() != null) {
                p.setContext(getContext());
            }
            Node t = p.parse();
            this.head = p.compile(t, getEffectiveConverterMap(), getEffectiveConverterSupplierMap());
            if (postCompileProcessor != null) {
                postCompileProcessor.process(context, head);
            }
            ConverterUtil.setContextForConverters(getContext(), head);
            ConverterUtil.startConverters(this.head);
            super.start();
        } catch (ScanException sce) {
            StatusManager sm = getContext().getStatusManager();
            sm.add(new ErrorStatus("Failed to parse pattern \"" + getPattern() + "\".", this, sce));
        }
    }

    public void setPostCompileProcessor(PostCompileProcessor<E> postCompileProcessor) {
        this.postCompileProcessor = postCompileProcessor;
    }

    /**
     *
     * @param head
     * @deprecated Use {@link ConverterUtil#setContextForConverters} instead. This
     *             method will be removed in future releases.
     */
    protected void setContextForConverters(Converter<E> head) {
        ConverterUtil.setContextForConverters(getContext(), head);
    }

    protected String writeLoopOnConverters(E event) {
        StringBuilder strBuilder = new StringBuilder(INTIAL_STRING_BUILDER_SIZE);
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

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String toString() {
        return this.getClass().getName() + "(\"" + getPattern() + "\")";
    }

    public Map<String, String> getInstanceConverterMap() {
        return instanceConverterMap;
    }

    public Map<String, Supplier<Converter<E>>> getInstanceConverterSupplierMap() {
        return instanceConverterSupplierMap;
    }

    protected String getPresentationHeaderPrefix() {
        return CoreConstants.EMPTY_STRING;
    }

    public boolean isOutputPatternAsHeader() {
        return outputPatternAsHeader;
    }

    public void setOutputPatternAsHeader(boolean outputPatternAsHeader) {
        this.outputPatternAsHeader = outputPatternAsHeader;
    }

    @Override
    public String getPresentationHeader() {
        if (outputPatternAsHeader)
            return getPresentationHeaderPrefix() + pattern;
        else
            return super.getPresentationHeader();
    }
}
