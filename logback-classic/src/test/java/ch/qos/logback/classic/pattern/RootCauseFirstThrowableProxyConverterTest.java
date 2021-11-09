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
package ch.qos.logback.classic.pattern;

import static ch.qos.logback.classic.util.TestHelper.makeNestedException;
import static ch.qos.logback.classic.util.TestHelper.positionOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * @author Tomasz Nurkiewicz
 * @since 2010-08-15, 18:34:21
 */
public class RootCauseFirstThrowableProxyConverterTest {

    private final LoggerContext context = new LoggerContext();
    private final ThrowableProxyConverter converter = new RootCauseFirstThrowableProxyConverter();
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter);

    @Before
    public void setUp() throws Exception {
        converter.setContext(context);
        converter.start();
    }

    private ILoggingEvent createLoggingEvent(final Throwable t) {
        return new LoggingEvent(this.getClass().getName(), context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME), Level.DEBUG, "test message", t, null);
    }

    @Test
    public void integration() {
        // given
        context.setPackagingDataEnabled(true);
        final PatternLayout pl = new PatternLayout();
        pl.setContext(context);
        pl.setPattern("%m%rEx%n");
        pl.start();

        // when
        final ILoggingEvent e = createLoggingEvent(new Exception("x"));
        final String result = pl.doLayout(e);

        // then
        // make sure that at least some package data was output
        final Pattern p = Pattern.compile("\\s*at .*?\\[.*?\\]");
        final Matcher m = p.matcher(result);
        int i = 0;
        while (m.find()) {
            i++;
        }
        assertThat(i).isGreaterThan(5);
    }

    @Test
    public void smoke() {
        // given
        final Exception exception = new Exception("smoke");
        exception.printStackTrace(printWriter);

        // when
        final ILoggingEvent le = createLoggingEvent(exception);
        String result = converter.convert(le);

        // then
        result = result.replace("common frames omitted", "more");
        result = result.replaceAll(" ~?\\[.*\\]", "");
        assertThat(result).isEqualTo(stringWriter.toString());
    }

    @Test
    public void nested() {
        // given
        final Throwable nestedException = makeNestedException(2);
        nestedException.printStackTrace(printWriter);

        // when
        final ILoggingEvent le = createLoggingEvent(nestedException);
        final String result = converter.convert(le);

        // then
        assertThat(result).startsWith("java.lang.Exception: nesting level=0");
        assertThat(positionOf("nesting level=0").in(result)).isLessThan(positionOf("nesting level =1").in(result));
        assertThat(positionOf("nesting level =1").in(result)).isLessThan(positionOf("nesting level =2").in(result));
    }

    @Test
    public void cyclicCause() {
        final Exception e = new Exception("foo");
        final Exception e2 = new Exception(e);
        e.initCause(e2);
        final ILoggingEvent le = createLoggingEvent(e);
        final String result = converter.convert(le);

        assertThat(result).startsWith("[CIRCULAR REFERENCE: java.lang.Exception: foo]");
    }


    @Test
    public void cyclicSuppressed() {
        final Exception e = new Exception("foo");
        final Exception e2 = new Exception(e);
        e.addSuppressed(e2);
        final ILoggingEvent le = createLoggingEvent(e);
        final String result = converter.convert(le);

        assertThat(result).startsWith("java.lang.Exception: foo");
        final String circular = "Suppressed: [CIRCULAR REFERENCE: java.lang.Exception: foo]";
        final String wrapped = "Wrapped by: java.lang.Exception: java.lang.Exception: foo";

        assertThat(positionOf(circular).in(result)).isLessThan(positionOf(wrapped).in(result));
    }


}
