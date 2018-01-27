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
package ch.qos.logback.classic.net;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.boolex.OnErrorEvaluator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.SMTPAppenderBase;
import org.slf4j.Marker;

/**
 * Send an e-mail when a specific logging event occurs, typically on errors or
 * fatal errors.
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#SMTPAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SMTPAppender extends SMTPAppenderBase<ILoggingEvent> {

    // value "%logger{20} - %m" is referenced in the docs!
    static final String DEFAULT_SUBJECT_PATTERN = "%logger{20} - %m";

    private boolean includeCallerData = false;

    /**
     * The default constructor will instantiate the appender with a
     * {@link EventEvaluator} that will trigger on events with level
     * ERROR or higher.
     */
    public SMTPAppender() {

    }

    public void start() {
        if (eventEvaluator == null) {
            OnErrorEvaluator onError = new OnErrorEvaluator();
            onError.setContext(getContext());
            onError.setName("onError");
            onError.start();
            this.eventEvaluator = onError;
        }
        super.start();
    }

    /**
     * Use the parameter as the {@link
     * EventEvaluator} for this SMTPAppender.
     */
    public SMTPAppender(EventEvaluator<ILoggingEvent> eventEvaluator) {
        this.eventEvaluator = eventEvaluator;
    }

    /**
     * Perform SMTPAppender specific appending actions, mainly adding the event to
     * a cyclic buffer.
     */
    protected void subAppend(CyclicBuffer<ILoggingEvent> cb, ILoggingEvent event) {
        if (includeCallerData) {
            event.getCallerData();
        }
        event.prepareForDeferredProcessing();
        cb.add(event);
    }

    @Override
    protected void fillBuffer(CyclicBuffer<ILoggingEvent> cb, StringBuffer sbuf) {
        int len = cb.length();
        for (int i = 0; i < len; i++) {
            ILoggingEvent event = cb.get();
            sbuf.append(layout.doLayout(event));
        }
    }

    protected boolean eventMarksEndOfLife(ILoggingEvent eventObject) {
        Marker marker = eventObject.getMarker();
        if (marker == null)
            return false;

        return marker.contains(ClassicConstants.FINALIZE_SESSION_MARKER);
    }

    @Override
    protected Layout<ILoggingEvent> makeSubjectLayout(String subjectStr) {
        if (subjectStr == null) {
            subjectStr = DEFAULT_SUBJECT_PATTERN;
        }
        PatternLayout pl = new PatternLayout();
        pl.setContext(getContext());
        pl.setPattern(subjectStr);
        // we don't want a ThrowableInformationConverter appended
        // to the end of the converter chain
        // This fixes issue LBCLASSIC-67
        pl.setPostCompileProcessor(null);
        pl.start();
        return pl;
    }

    protected PatternLayout makeNewToPatternLayout(String toPattern) {
        PatternLayout pl = new PatternLayout();
        pl.setPattern(toPattern + "%nopex");
        return pl;
    }

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }
}
