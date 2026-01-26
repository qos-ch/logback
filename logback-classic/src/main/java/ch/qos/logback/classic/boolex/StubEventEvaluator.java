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

package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;

import java.util.ArrayList;
import java.util.List;

public class StubEventEvaluator  extends EventEvaluatorBase<ILoggingEvent> {

    static public final String MSG_0 = "This class is a stub for JaninoEventEvaluator which was removed in logback version 1.5.13";
    static public final String MSG_1 = "You can migrate existing configurations to Java-only equivalents with the \"Janino Expression migrator\" tool at:";
    static public final String MSG_2 ="https://logback.qos.ch/translator/services/janinoExpressionMigrator.html";

    protected List<Matcher> matcherList = new ArrayList<>();
    String expression;

    @Override
    public void start() {
        stop();
        addWarn(MSG_0);
        addWarn(MSG_1);
        addWarn(MSG_2);
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        return false;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void addMatcher(Matcher matcher) {
        matcherList.add(matcher);
    }

}
