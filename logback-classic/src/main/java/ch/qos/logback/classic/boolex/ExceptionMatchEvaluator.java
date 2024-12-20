/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

/**
 * A simple {@link ch.qos.logback.core.boolex.EventEvaluator} that checks whether the
 * logging event being evaluated has a throwable of the same class as specified by the
 * {@link #exceptionClass} parameter.
 *
 * <p>Here is a </p>
 * <pre>
 *  &lt;configuration>
 *     &lt;import class="ch.qos.logback.classic.encoder.PatternLayoutEncoder"/>
 *     &lt;import class="ch.qos.logback.core.filter.EvaluatorFilter"/>
 *     &lt;import class="ch.qos.logback.classic.boolex.ExceptionMatchEvaluator"/>
 *     &lt;import class="ch.qos.logback.core.ConsoleAppender"/>
 *
 *     &lt;appender name="CONSOLE" class="ConsoleAppender">
 *         &lt;filter class="EvaluatorFilter">
 *             &lt;evaluator class="ExceptionMatchEvaluator">
 *                 &lt;exceptionClass>java.lang.RuntimeException&lt;/exceptionClass>
 *             &lt;/evaluator>
 *             &lt;OnMismatch>DENY&lt;/OnMismatch>
 *             &lt;OnMatch>NEUTRAL&lt;/OnMatch>
 *         &lt;/filter>
 *
 *         &lt;encoder class="PatternLayoutEncoder">
 *             &lt;pattern>%-4relative [%thread] %-5level %logger -%kvp -%msg%n&lt;/pattern>
 *         &lt;/encoder>
 *     &lt;/appender>
 *
 *     &lt;root level="INFO">
 *         &lt;appender-ref ref="CONSOLE"/>
 *     &lt;/root>
 * &lt;/configuration>
 *
 *
 * </pre>
 */
public class ExceptionMatchEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    String exceptionClass;
    private boolean start = false;

    public void start() {
        if (exceptionClass == null) {
            addError("The exceptionClass must be set");
            return;
        }
        start = true;
    }

    public void stop() {
        start = false;
    }

    public boolean isStarted() {
        return start;
    }

    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {

        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) {
            return false;
        }
        return throwableProxy.getClassName().equalsIgnoreCase(exceptionClass);
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

}
