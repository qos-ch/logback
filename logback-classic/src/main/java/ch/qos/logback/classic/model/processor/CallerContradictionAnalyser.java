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

    static final String APPENDER_TO_CALLER_INSTRUCTION_MAP_KEY = "APPENDER_TO_CALLER_INSTRUCTION_MAP_KEY";

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

        Map<String, CallerInstructionLogic.Instruction> appenderNameToCallerInstructionMap
                = getAppenderNameToCallerInstructionMap(mic);

        String originalClassName = appenderModel.getClassName();
        String className = mic.getImport(originalClassName);
        String appenderName = mic.subst(appenderModel.getName());

        if (AsyncAppender.class.getName().equals(className)) {

            if (isIncludeCallerDataTrue(mic, appenderModel)) {
                appenderNameToCallerInstructionMap.put(appenderName, CallerInstructionLogic.Instruction.PREPROCESS_WANT);
            } else {
                appenderNameToCallerInstructionMap.put(appenderName, CallerInstructionLogic.Instruction.DO_NOT_WANT);
            }

        }

        if (hasCallerDataConverters(appenderModel)) {
            appenderNameToCallerInstructionMap.put(appenderName, CallerInstructionLogic.Instruction.DIRECT_WANT);
        }
    }

    /**
     * Note that in AsyncAppender includeCallerData is false by default, so if
     * the tag is absent we treat it as false.
     *
     * @param mic
     * @param appenderModel
     * @return
     */
    private boolean isIncludeCallerDataTrue(ModelInterpretationContext mic,
            AppenderModel appenderModel) {
        for (Model child : appenderModel.getSubModels()) {
            if (child instanceof ImplicitModel
                    && "includeCallerData".equalsIgnoreCase(child.getTag())) {
                String value = mic.subst(((ImplicitModel) child).getBodyText());
                return "true".equalsIgnoreCase(value);
            }
        }
        return false; // absent → default false in AsyncAppender
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
    static Map<String, CallerInstructionLogic.Instruction> getAppenderNameToCallerInstructionMap(ModelInterpretationContext mic) {
        Map<String, CallerInstructionLogic.Instruction> map =
                (Map<String, CallerInstructionLogic.Instruction>) mic.getObjectMap().get(APPENDER_TO_CALLER_INSTRUCTION_MAP_KEY);
        if (map == null) {
            map = new LinkedHashMap<>();
            mic.getObjectMap().put(APPENDER_TO_CALLER_INSTRUCTION_MAP_KEY, map);
        }
        return map;
    }
}
