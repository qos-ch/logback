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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;

public class JaninoEventEvaluator extends JaninoEventEvaluatorBase<ILoggingEvent> {

    public final static String IMPORT_LEVEL = "import ch.qos.logback.classic.Level;\r\n";

    public final static List<String> DEFAULT_PARAM_NAME_LIST = new ArrayList<String>();
    public final static List<Class> DEFAULT_PARAM_TYPE_LIST = new ArrayList<Class>();

    static {
        DEFAULT_PARAM_NAME_LIST.add("DEBUG");
        DEFAULT_PARAM_NAME_LIST.add("INFO");
        DEFAULT_PARAM_NAME_LIST.add("WARN");
        DEFAULT_PARAM_NAME_LIST.add("ERROR");

        DEFAULT_PARAM_NAME_LIST.add("event");
        DEFAULT_PARAM_NAME_LIST.add("message");

        DEFAULT_PARAM_NAME_LIST.add("formattedMessage");
        DEFAULT_PARAM_NAME_LIST.add("logger");
        DEFAULT_PARAM_NAME_LIST.add("loggerContext");
        DEFAULT_PARAM_NAME_LIST.add("level");
        DEFAULT_PARAM_NAME_LIST.add("timeStamp");
        DEFAULT_PARAM_NAME_LIST.add("marker");
        DEFAULT_PARAM_NAME_LIST.add("mdc");
        DEFAULT_PARAM_NAME_LIST.add("throwableProxy");
        DEFAULT_PARAM_NAME_LIST.add("throwable");

        DEFAULT_PARAM_TYPE_LIST.add(int.class);
        DEFAULT_PARAM_TYPE_LIST.add(int.class);
        DEFAULT_PARAM_TYPE_LIST.add(int.class);
        DEFAULT_PARAM_TYPE_LIST.add(int.class);

        DEFAULT_PARAM_TYPE_LIST.add(ILoggingEvent.class);
        DEFAULT_PARAM_TYPE_LIST.add(String.class);
        DEFAULT_PARAM_TYPE_LIST.add(String.class);
        DEFAULT_PARAM_TYPE_LIST.add(String.class);
        DEFAULT_PARAM_TYPE_LIST.add(LoggerContextVO.class);
        DEFAULT_PARAM_TYPE_LIST.add(int.class);
        DEFAULT_PARAM_TYPE_LIST.add(long.class);
        DEFAULT_PARAM_TYPE_LIST.add(Marker.class);
        DEFAULT_PARAM_TYPE_LIST.add(Map.class);
        DEFAULT_PARAM_TYPE_LIST.add(IThrowableProxy.class);
        DEFAULT_PARAM_TYPE_LIST.add(Throwable.class);
    }

    protected String getDecoratedExpression() {
        String expression = getExpression();
        if (!expression.contains("return")) {
            expression = "return " + expression + ";";
            addInfo("Adding [return] prefix and a semicolon suffix. Expression becomes [" + expression + "]");
            addInfo("See also " + CoreConstants.CODES_URL + "#block");

        }
        return IMPORT_LEVEL + expression;
    }

    protected String[] getParameterNames() {
        List<String> fullNameList = new ArrayList<String>();
        fullNameList.addAll(DEFAULT_PARAM_NAME_LIST);

        for (int i = 0; i < matcherList.size(); i++) {
            Matcher m = (Matcher) matcherList.get(i);
            fullNameList.add(m.getName());
        }

        return (String[]) fullNameList.toArray(CoreConstants.EMPTY_STRING_ARRAY);
    }

    protected Class[] getParameterTypes() {
        List<Class> fullTypeList = new ArrayList<Class>();
        fullTypeList.addAll(DEFAULT_PARAM_TYPE_LIST);
        for (int i = 0; i < matcherList.size(); i++) {
            fullTypeList.add(Matcher.class);
        }
        return (Class[]) fullTypeList.toArray(CoreConstants.EMPTY_CLASS_ARRAY);
    }

    protected Object[] getParameterValues(ILoggingEvent loggingEvent) {
        final int matcherListSize = matcherList.size();

        int i = 0;
        Object[] values = new Object[DEFAULT_PARAM_NAME_LIST.size() + matcherListSize];

        values[i++] = Level.DEBUG_INTEGER;
        values[i++] = Level.INFO_INTEGER;
        values[i++] = Level.WARN_INTEGER;
        values[i++] = Level.ERROR_INTEGER;

        values[i++] = loggingEvent;
        values[i++] = loggingEvent.getMessage();
        values[i++] = loggingEvent.getFormattedMessage();
        values[i++] = loggingEvent.getLoggerName();
        values[i++] = loggingEvent.getLoggerContextVO();
        values[i++] = loggingEvent.getLevel().toInteger();
        values[i++] = loggingEvent.getTimeStamp();
        // In order to avoid NullPointerException, we could push a dummy marker if
        // the event's marker is null. However, this would surprise user who
        // expect to see a null marker instead of a dummy one.
        values[i++] = loggingEvent.getMarker();
        values[i++] = loggingEvent.getMDCPropertyMap();

        IThrowableProxy iThrowableProxy = loggingEvent.getThrowableProxy();

        if (iThrowableProxy != null) {
            values[i++] = iThrowableProxy;
            if (iThrowableProxy instanceof ThrowableProxy) {
                values[i++] = ((ThrowableProxy) iThrowableProxy).getThrowable();
            } else {
                values[i++] = null;
            }
        } else {
            values[i++] = null;
            values[i++] = null;
        }

        for (int j = 0; j < matcherListSize; j++) {
            values[i++] = (Matcher) matcherList.get(j);
        }

        return values;
    }

}
