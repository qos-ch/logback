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

package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.Model;

import java.util.HashSet;
import java.util.Set;

/**
 * The AppenderAvailabilityAnalyser class is responsible for analyzing the availability
 * of appenders. By available, we mean whether an appender with a given name is declared
 * somewhere in the configuration. This availability information is later used by
 * AppenderRefModelHandler to attempt to attach only those appenders that were previously
 * declared.
 */
@PhaseIndicator(phase = ProcessingPhase.DEPENDENCY_ANALYSIS)
public class AppenderDeclarationAnalyser extends ModelHandlerBase {

    static final String DECLARED_APPENDER_NAME_SET_KEY = "DECLARED_APPENDER_NAME_SET";


    public AppenderDeclarationAnalyser(Context context) {
        super(context);
    }

    @Override
    protected Class<AppenderModel> getSupportedModelClass() {
        return AppenderModel.class;
    }


    @Override
    public void handle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        AppenderModel appenderModel = (AppenderModel) model;
        String appenderName = mic.subst(appenderModel.getName());

        addAppenderDeclaration(mic, appenderName);
    }


    static public Set<String> getAppenderNameSet(ModelInterpretationContext mic) {
        Set<String> set = (Set<String>) mic.getObjectMap().get(DECLARED_APPENDER_NAME_SET_KEY);
        if(set == null) {
            set = new HashSet<>();
            mic.getObjectMap().put(DECLARED_APPENDER_NAME_SET_KEY, set);
        }
        return set;
    }

    static public void addAppenderDeclaration(ModelInterpretationContext mic, String appenderName) {
        Set<String> set = getAppenderNameSet(mic);
        set.add(appenderName);
    }


    static public boolean isAppenderDeclared(ModelInterpretationContext mic, String appenderName) {
        Set<String> set = getAppenderNameSet(mic);
        return set.contains(appenderName);
    }

}
