/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.classic.model.processor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.AppenderRefModel;
import ch.qos.logback.core.model.ImplicitModel;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;

/**
 * Dependency-analysis pass over every {@link AppenderModel}: records which
 * appenders suppress caller data (AsyncAppender with
 * {@code includeCallerData=false} / default) and which appenders need it
 * (pattern contains a caller-data converter).
 *
 * <p>The contradiction check is performed by {@link CallerContradictionWarnAnalyser}
 * in its {@code postHandle()} on the enclosing {@code ConfigurationModel}, after
 * all appender models have been visited.</p>
 *
 * @since 1.6.2
 * @see CallerContradictionWarnAnalyser
 */
@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class CallerContradictionAnalyser extends ModelHandlerBase {

    static final String ASYNC_SUPPRESSES_MAP_KEY = "ASYNC_SUPPRESSES_CALLER_DATA_MAP";
    static final String NEEDS_CALLER_DATA_SET_KEY = "NEEDS_CALLER_DATA_SET";

    /**
     * Matches caller-data converter words in a logback pattern string.
     * Single-char forms (%C class, %M method, %L line, %F file, %l location) are
     * case-sensitive; multi-char aliases (class, method, line, file, caller) are
     * case-insensitive and unique enough to match without case sensitivity issues.
     * Negative lookahead prevents partial matches like %Msg being flagged.
     */
    static final Pattern CALLER_PATTERN = Pattern.compile(
            "%([CMLFl]|caller|class|method|line|file)(?![a-zA-Z])");

    public CallerContradictionAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<AppenderModel> getSupportedModelClass() {
        return AppenderModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        AppenderModel appenderModel = (AppenderModel) model;

        String originalClassName = appenderModel.getClassName();
        String className = mic.getImport(originalClassName);
        String appenderName = mic.subst(appenderModel.getName());

        if (AsyncAppender.class.getName().equals(className)) {
            if (isIncludeCallerDataAbsentOrFalse(mic, appenderModel)) {
                Set<String> refs = collectAppenderRefNames(mic, appenderModel);
                getAsyncSuppressesMap(mic).put(appenderName, refs);
            }
        }

        if (hasCallerDataConverters(appenderModel)) {
            getNeedsCallerDataSet(mic).add(appenderName);
        }
    }

    private boolean isIncludeCallerDataAbsentOrFalse(ModelInterpretationContext mic,
            AppenderModel appenderModel) {
        for (Model child : appenderModel.getSubModels()) {
            if (child instanceof ImplicitModel
                    && "includeCallerData".equalsIgnoreCase(child.getTag())) {
                String value = mic.subst(((ImplicitModel) child).getBodyText());
                return !"true".equalsIgnoreCase(value);
            }
        }
        return true; // absent → default false in AsyncAppender
    }

    private Set<String> collectAppenderRefNames(ModelInterpretationContext mic,
            AppenderModel appenderModel) {
        Set<String> refs = new LinkedHashSet<>();
        for (Model child : appenderModel.getSubModels()) {
            if (child instanceof AppenderRefModel) {
                String ref = mic.subst(((AppenderRefModel) child).getRef());
                refs.add(ref);
            }
        }
        return refs;
    }

    private boolean hasCallerDataConverters(AppenderModel appenderModel) {
        return collectPatternBodyTexts(appenderModel).stream()
                .anyMatch(p -> CALLER_PATTERN.matcher(p).find());
    }

    private Set<String> collectPatternBodyTexts(Model model) {
        Set<String> patterns = new LinkedHashSet<>();
        collectPatternBodyTextsRecursive(model, patterns);
        return patterns;
    }

    private void collectPatternBodyTextsRecursive(Model model, Set<String> out) {
        if (model instanceof ImplicitModel && "pattern".equalsIgnoreCase(model.getTag())) {
            String body = model.getBodyText();
            if (body != null) {
                out.add(body);
            }
        }
        for (Model child : model.getSubModels()) {
            collectPatternBodyTextsRecursive(child, out);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Set<String>> getAsyncSuppressesMap(ModelInterpretationContext mic) {
        Map<String, Set<String>> map =
                (Map<String, Set<String>>) mic.getObjectMap().get(ASYNC_SUPPRESSES_MAP_KEY);
        if (map == null) {
            map = new LinkedHashMap<>();
            mic.getObjectMap().put(ASYNC_SUPPRESSES_MAP_KEY, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    static Set<String> getNeedsCallerDataSet(ModelInterpretationContext mic) {
        Set<String> set = (Set<String>) mic.getObjectMap().get(NEEDS_CALLER_DATA_SET_KEY);
        if (set == null) {
            set = new LinkedHashSet<>();
            mic.getObjectMap().put(NEEDS_CALLER_DATA_SET_KEY, set);
        }
        return set;
    }
}
