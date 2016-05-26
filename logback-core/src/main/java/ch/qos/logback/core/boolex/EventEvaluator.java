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
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * Evaluates whether a given an event matches user-specified criteria.
 * 
 * <p>
 * Implementations are free to evaluate the event as they see fit. In
 * particular, the evaluation results <em>may</em> depend on previous events.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */

public interface EventEvaluator<E> extends ContextAware, LifeCycle {

    /**
     * Evaluates whether the event passed as parameter matches some user-specified
     * criteria.
     * 
     * <p>
     * The <code>Evaluator</code> is free to evaluate the event as it pleases. In
     * particular, the evaluation results <em>may</em> depend on previous events.
     * 
     * @param event
     *          The event to evaluate
     * @return true if there is a match, false otherwise.
     * @throws NullPointerException
     *           can be thrown in presence of null values
     * @throws EvaluationException
     *           may be thrown during faulty evaluation
     */
    boolean evaluate(E event) throws NullPointerException, EvaluationException;

    /**
     * Evaluators are named entities.
     * 
     * @return The name of this evaluator.
     */
    String getName();

    /**
     * Evaluators are named entities.
     */
    void setName(String name);
}
