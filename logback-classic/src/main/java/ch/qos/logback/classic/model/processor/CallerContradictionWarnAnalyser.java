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

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.qos.logback.classic.model.ConfigurationModel;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.processor.ModelHandlerBase;
import ch.qos.logback.core.model.processor.ModelHandlerException;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.model.processor.PhaseIndicator;
import ch.qos.logback.core.model.processor.ProcessingPhase;
import ch.qos.logback.core.status.Status;

/**
 * Dependency-analysis handler for {@link ConfigurationModel} that warns when a
 * configuration contains contradictory caller-data instructions, for example an
 * {@code AsyncAppender} or {@code SocketAppender} with
 * {@code includeCallerData=false} (the default) alongside an appender whose
 * pattern uses a caller-data converter ({@code %C}, {@code %M}, {@code %L},
 * {@code %F}, {@code %l}, {@code %class}, {@code %method}, {@code %line},
 * {@code %file}, {@code %caller}).
 *
 * <p>All work is done in {@link #postHandle} so that it runs after every child
 * {@link ch.qos.logback.core.model.AppenderModel} has been visited by
 * {@link CallerContradictionAnalyser}, regardless of declaration order.</p>
 *
 * @since 1.6.2
 * @see CallerContradictionAnalyser
 */
@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class CallerContradictionWarnAnalyser extends ModelHandlerBase {

    static final String CONTRADICTION_MESSAGE =
            "AsyncAppender [%s] has includeCallerData=false (the default) but wraps appender [%s] "
            + "whose pattern uses a caller-data converter (%%C/%%M/%%L/%%F/%%l/%%class/%%method/%%line/%%file/%%caller). "
            + "Caller data will not be available for that appender. "
            + "Consider setting <includeCallerData>true</includeCallerData> on AsyncAppender [%s].";

    public CallerContradictionWarnAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<ConfigurationModel> getSupportedModelClass() {
        return ConfigurationModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        // no-op; all work is in postHandle after children are visited
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        Map<String, CallerInstructionLogic.Instruction> appenderNameToCallerInstructionMap =
                CallerContradictionAnalyser.getAppenderNameToCallerInstructionMap(mic);

        CallerInstructionLogic callerInstructionLogic = new CallerInstructionLogic();

        List<Status> messages = callerInstructionLogic.contradiction(appenderNameToCallerInstructionMap);

        messages.forEach(message -> addStatus(message));


    }
}
