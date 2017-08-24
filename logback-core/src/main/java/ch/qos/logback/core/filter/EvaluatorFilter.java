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
package ch.qos.logback.core.filter;

import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The value of the {@link #onMatch} and {@link #onMismatch} attributes is set
 * to {@link FilterReply#NEUTRAL}, so that a badly configured evaluator filter does
 * not disturb the functioning of the filter chain. 
 * 
 * <p>It is expected that one of the two attributes will have its value changed
 * to {@link FilterReply#ACCEPT} or {@link FilterReply#DENY}. That way, it is possible to
 * decide if a given result must be returned after the evaluation either failed
 * or succeeded.
 * 
 * 
 * <p> For more information about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class EvaluatorFilter<E> extends AbstractMatcherFilter<E> {

    EventEvaluator<E> evaluator;

    @Override
    public void start() {
        if (evaluator != null) {
            super.start();
        } else {
            addError("No evaluator set for filter " + this.getName());
        }
    }

    public EventEvaluator<E> getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(EventEvaluator<E> evaluator) {
        this.evaluator = evaluator;
    }

    public FilterReply decide(E event) {
        // let us not throw an exception
        // see also bug #17.
        if (!isStarted() || !evaluator.isStarted()) {
            return FilterReply.NEUTRAL;
        }
        try {
            if (evaluator.evaluate(event)) {
                return onMatch;
            } else {
                return onMismatch;
            }
        } catch (EvaluationException e) {
            addError("Evaluator " + evaluator.getName() + " threw an exception", e);
            return FilterReply.NEUTRAL;
        }
    }

}
