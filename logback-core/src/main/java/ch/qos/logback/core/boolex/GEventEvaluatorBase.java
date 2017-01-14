/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.boolex;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.util.FileUtil;
import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;

public class GEventEvaluatorBase<E extends DeferredProcessingAware>
    extends EventEvaluatorBase<E> {

    String expression;

    IEvaluator<E> delegateEvaluator;
    Script script;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public void start() {
        int errors = 0;
        if (expression == null || expression.length() == 0) {
            addError("Empty expression");
            return;
        } else {
            addInfo("Expresssion to evaluate [" + expression + "]");
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

    @Override
    public boolean evaluate(E event) throws NullPointerException, EvaluationException {
        if (delegateEvaluator == null) {
            return false;
        }
        return delegateEvaluator.doEvaluate(event);
    }

}
