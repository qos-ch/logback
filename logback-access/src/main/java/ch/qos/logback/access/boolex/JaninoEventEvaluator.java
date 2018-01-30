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
package ch.qos.logback.access.boolex;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;

public class JaninoEventEvaluator extends JaninoEventEvaluatorBase<IAccessEvent> {

    public final static List<String> DEFAULT_PARAM_NAME_LIST = new ArrayList<>();
    public final static List<Class<?>> DEFAULT_PARAM_TYPE_LIST = new ArrayList<>();

    static {
        DEFAULT_PARAM_NAME_LIST.add("event");
        DEFAULT_PARAM_TYPE_LIST.add(IAccessEvent.class);
    }

    @Override
    protected String getDecoratedExpression() {
        String expression = getExpression();
        if (!expression.contains("return")) {
            expression = "return " + expression + ";";
            addInfo("Adding [return] prefix and a semicolon suffix. Expression becomes [" + expression + "]");
            addInfo("See also " + CoreConstants.CODES_URL + "#block");
        }
        return expression;
    }

    @Override
    protected String[] getParameterNames() {
        List<String> fullNameList = new ArrayList<String>();
        fullNameList.addAll(DEFAULT_PARAM_NAME_LIST);

        for (int i = 0; i < matcherList.size(); i++) {
            Matcher m = (Matcher) matcherList.get(i);
            fullNameList.add(m.getName());
        }

        return (String[]) fullNameList.toArray(CoreConstants.EMPTY_STRING_ARRAY);
    }

    @Override
    protected Class<?>[] getParameterTypes() {
        List<Class<?>> fullTypeList = new ArrayList<>();
        fullTypeList.addAll(DEFAULT_PARAM_TYPE_LIST);
        for (int i = 0; i < matcherList.size(); i++) {
            fullTypeList.add(Matcher.class);
        }
        return (Class[]) fullTypeList.toArray(CoreConstants.EMPTY_CLASS_ARRAY);
    }

    @Override
    protected Object[] getParameterValues(IAccessEvent accessEvent) {
        final int matcherListSize = matcherList.size();

        int i = 0;
        Object[] values = new Object[DEFAULT_PARAM_NAME_LIST.size() + matcherListSize];

        values[i++] = accessEvent;

        for (int j = 0; j < matcherListSize; j++) {
            values[i++] = matcherList.get(j);
        }

        return values;
    }

}
