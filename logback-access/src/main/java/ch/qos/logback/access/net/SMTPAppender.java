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
package ch.qos.logback.access.net;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.net.SMTPAppenderBase;

/**
 * Send an e-mail when a specific access event occurs, typically when
 * certain pages are accessed.
 * 
 * For more information about this appender, please refer to the online manual at
 * http://logback.qos.ch/manual/appenders.html#AccessSMTPAppender
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * 
 */
public class SMTPAppender extends SMTPAppenderBase<IAccessEvent> {

    static final String DEFAULT_SUBJECT_PATTERN = "%m";

    /**
     * The default constructor will instantiate the appender with a
     * {@link EventEvaluator} that will trigger on events with level
     * ERROR or higher.
     */
    public SMTPAppender() {
    }

    /**
     * Use <code>evaluator</code> passed as parameter as the {@link
     * EventEvaluator} for this SMTPAppender.
     */
    public SMTPAppender(EventEvaluator<IAccessEvent> evaluator) {
        this.eventEvaluator = evaluator;
    }

    /**
     * Perform SMTPAppender specific appending actions, mainly adding the event to
     * the appropriate cyclic buffer.
     */
    @Override
    protected void subAppend(CyclicBuffer<IAccessEvent> cb, IAccessEvent event) {
        cb.add(event);
    }

    @Override
    protected void fillBuffer(CyclicBuffer<IAccessEvent> cb, StringBuffer sbuf) {
        int len = cb.length();
        for (int i = 0; i < len; i++) {
            // sbuf.append(MimeUtility.encodeText(layout.format(cb.getOrCreate())));
            IAccessEvent event = cb.get();
            sbuf.append(layout.doLayout(event));
        }
    }

    @Override
    protected Layout<IAccessEvent> makeSubjectLayout(String subjectStr) {
        if (subjectStr == null) {
            subjectStr = DEFAULT_SUBJECT_PATTERN;
        }
        PatternLayout pl = new PatternLayout();
        pl.setPattern(subjectStr);
        pl.start();
        return pl;
    }

    @Override
    protected PatternLayout makeNewToPatternLayout(String toPattern) {
        PatternLayout pl = new PatternLayout();
        pl.setPattern(toPattern);
        return pl;
    }

    @Override
    protected boolean eventMarksEndOfLife(IAccessEvent eventObject) {
        return false;
    }

}
