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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.janino.ScriptEvaluator;

/**
 * Abstract class which sets the groundwork for janino based evaluations.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 * @param <E> event type
 */
abstract public class JaninoEventEvaluatorBase<E> extends EventEvaluatorBase<E> {

    static Class<?> EXPRESSION_TYPE = boolean.class;
    static Class<?>[] THROWN_EXCEPTIONS = new Class[1];

    static public final int ERROR_THRESHOLD = 4;
    static {
        THROWN_EXCEPTIONS[0] = EvaluationException.class;
    }

    private String expression;

    ScriptEvaluator scriptEvaluator;
    private int errorCount = 0;

    abstract protected String getDecoratedExpression();

    abstract protected String[] getParameterNames();

    abstract protected Class<?>[] getParameterTypes();

    abstract protected Object[] getParameterValues(E event);

    protected List<Matcher> matcherList = new ArrayList<Matcher>();

    @Override
    public void start() {
        try {
            assert context != null;
            scriptEvaluator = new ScriptEvaluator(getDecoratedExpression(), EXPRESSION_TYPE, getParameterNames(), getParameterTypes(), THROWN_EXCEPTIONS);
            super.start();
        } catch (Exception e) {
            addError("Could not start evaluator with expression [" + expression + "]", e);
        }
    }

    public boolean evaluate(E event) throws EvaluationException {
        if (!isStarted()) {
            throw new IllegalStateException("Evaluator [" + name + "] was called in stopped state");
        }
        try {
            Boolean result = (Boolean) scriptEvaluator.evaluate(getParameterValues(event));
            return result.booleanValue();
        } catch (Exception ex) {
            errorCount++;
            if (errorCount >= ERROR_THRESHOLD) {
                stop();
            }
            throw new EvaluationException("Evaluator [" + name + "] caused an exception", ex);
        }
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

    public List<Matcher> getMatcherList() {
        return matcherList;
    }
}
