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
package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import ch.qos.logback.core.util.FileUtil;
import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class GEventEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    String expression;

    IEvaluator delegateEvaluator;
    Script script;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void start() {
        int errors = 0;
        if (expression == null || expression.length() == 0) {
            addError("Empty expression");
            return;
        } else {
            addInfo("Expression to evaluate [" + expression + "]");
        }

        ClassLoader classLoader = getClass().getClassLoader();
        String currentPackageName = this.getClass().getPackage().getName();
        currentPackageName = currentPackageName.replace('.', '/');

        FileUtil fileUtil = new FileUtil(getContext());
        String scriptText = fileUtil.resourceAsString(classLoader, currentPackageName + "/EvaluatorTemplate.groovy");
        if (scriptText == null) {
            return;
        }

        // insert the expression into script text
        scriptText = scriptText.replace("//EXPRESSION", expression);

        GroovyClassLoader gLoader = new GroovyClassLoader(classLoader);
        try {
            Class scriptClass = gLoader.parseClass(scriptText);

            GroovyObject goo = (GroovyObject) scriptClass.newInstance();
            delegateEvaluator = (IEvaluator) goo;

        } catch (CompilationFailedException cfe) {
            addError("Failed to compile expression [" + expression + "]", cfe);
            errors++;
        } catch (Exception e) {
            addError("Failed to compile expression [" + expression + "]", e);
            errors++;
        }
        if (errors == 0)
            super.start();
    }

    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
        if (delegateEvaluator == null) {
            return false;
        }
        return delegateEvaluator.doEvaluate(event);
    }

}
