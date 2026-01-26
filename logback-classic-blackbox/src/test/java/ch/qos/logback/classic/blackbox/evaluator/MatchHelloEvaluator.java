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

package ch.qos.logback.classic.blackbox.evaluator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

public class MatchHelloEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    String checkForInclusion;

    public void start() {
        if (checkForInclusion != null) {
            start();
        }
    }

    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        if (!isStarted()) {
            return false;
        }

        String message = event.getMessage();
        boolean result = message.contains(checkForInclusion);
        return result;
    }

    public String getCheckForInclusion() {
        return checkForInclusion;
    }

    public void setCheckForInclusion(String checkForInclusion) {
        this.checkForInclusion = checkForInclusion;
    }

}
