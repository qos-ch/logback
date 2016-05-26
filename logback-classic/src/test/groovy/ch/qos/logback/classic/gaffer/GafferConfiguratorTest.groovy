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
package ch.qos.logback.classic.gaffer

import ch.qos.logback.classic.ClassicTestConstants
import ch.qos.logback.classic.LoggerContext
import org.junit.Before
import ch.qos.logback.core.testUtil.RandomUtil
import org.junit.Ignore
import org.junit.Test
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.Level
import static junit.framework.Assert.assertNotNull
import static junit.framework.Assert.assertEquals
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.testUtil.StringListAppender
import ch.qos.logback.classic.testUtil.SampleConverter
import ch.qos.logback.core.util.StatusPrinter

import ch.qos.logback.classic.boolex.JaninoEventEvaluator
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.core.boolex.Matcher
import static org.junit.Assert.assertTrue

/**
 * @author Ceki G&uuml;c&uuml;
 */
class GafferConfiguratorTest {

    LoggerContext context = new LoggerContext();
    Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME)
    Logger logger = context.getLogger(this.getClass())
    int diff = RandomUtil.getPositiveInt();
    GafferConfigurator configurator = new GafferConfigurator(context);
    final shouldFail = new GroovyTestCase().&shouldFail

    @Before
    void setUp() {

    }

    @Test
    void smoke() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "smoke.groovy")
        String dslText = file.text
        configurator.run dslText
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        assertEquals(Level.WARN, root.level)
        assertNotNull(root.getAppender("C"))
        ConsoleAppender ca = root.getAppender("C")
        assertNotNull(ca.encoder)
        assertNotNull(ca.encoder.layout)
        PatternLayout layout = ca.encoder.layout
        assertEquals("%m%n", layout.pattern)
    }

    @Test
    void onTheFly() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "onTheFly.groovy")
        String dslText = file.text
        configurator.run dslText
    }

    @Test
    void contextName() {
        String dslText = "context.name = 'a'"
        configurator.run dslText
        assertEquals("a", context.name)
    }

    @Test
    void contextProperty() {
        String dslText = "context.putProperty('x', 'a')"
        configurator.run dslText
        assertEquals("a", context.getProperty("x"))
    }

    @Test
    void conversionRule() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "conversionRule.groovy")
        String dslText = file.text
        configurator.run dslText

        StringListAppender<ILoggingEvent> sla = (StringListAppender<ILoggingEvent>) root.getAppender("LIST");
        assertNotNull(sla);
        assertEquals(0, sla.strList.size());

        assertEquals(Level.DEBUG, root.level);

        String msg = "Simon says";
        logger.debug(msg);
        StatusPrinter.print context
        assertEquals(1, sla.strList.size());
        assertEquals(SampleConverter.SAMPLE_STR + " - " + msg, sla.strList.get(0));
    }

    @Test
    void evaluatorWithMatcher() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "evaluatorWithMatcher.groovy")
        String dslText = file.text
        configurator.run dslText

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()

        EvaluatorFilter ef = ca.getCopyOfAttachedFiltersList()[0];
        assertTrue ef.isStarted()

        JaninoEventEvaluator jee = ef.evaluator
        assertTrue jee.isStarted()
        Matcher m = jee.matcherList[0]
        assertTrue m.isStarted()
    }

    @Test
    void propertyCascading0() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading0.groovy")
        String dslText = file.text
        configurator.run dslText

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()

        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void propertyCascading1() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading1.groovy")
        String dslText = file.text
        configurator.run dslText

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.getLayout().pattern)
    }

    @Test
    void propertyCascading2() {
        context.putProperty("p", "HELLO");
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading2.groovy")
        String dslText = file.text
        configurator.run dslText

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.getLayout().pattern)
    }


    @Test
    @Ignore
    void receiver() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading2.groovy")
        String dslText = file.text
        configurator.run dslText
    }

    @Test
    void appenderRefShouldWork() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "asyncAppender.groovy")
        configurator.run file.text

        def aa = (AsyncAppender) root.getAppender('STDOUT-ASYNC');
        assertTrue aa.isStarted()
        def stdout = (ConsoleAppender) aa.getAppender('STDOUT')
        assertNotNull stdout
    }

    @Test
    void appenderRefWithNonAppenderAttachable() {
        String message = shouldFail(IllegalArgumentException) {
            File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "appenderRefWithNonAppenderAttachable.groovy")
            configurator.run file.text
        }
        assertEquals message, "ch.qos.logback.core.ConsoleAppender does not implement ch.qos.logback.core.spi.AppenderAttachable."
    }
}
