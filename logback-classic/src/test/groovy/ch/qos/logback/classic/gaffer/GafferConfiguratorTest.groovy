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

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.ClassicTestConstants
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.boolex.JaninoEventEvaluator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.testUtil.SampleConverter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.boolex.Matcher
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker
import ch.qos.logback.core.testUtil.RandomUtil
import ch.qos.logback.core.testUtil.StringListAppender
import ch.qos.logback.core.util.StatusPrinter

import org.codehaus.groovy.control.CompilationFailedException

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertNotNull
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
    StatusChecker statusChecker = new StatusChecker(context);

    @Before
    void setUp() {

    }

    @Test
    void smoke() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "smoke.groovy")
        configurator.run file
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
        configurator.run file
    }

    @Test
    void contextName() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "contextName.groovy")
        configurator.run file

        assertEquals("a", context.name)
    }

    @Test
    void contextProperty() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "contextProperty.groovy")
        configurator.run file

        assertEquals("a", context.getProperty("x"))
    }

    @Test
    void conversionRule() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "conversionRule.groovy")
        configurator.run file

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
        configurator.run file

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
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()

        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void propertyCascading1() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading1.groovy")
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.getLayout().pattern)
    }

    @Test
    void propertyCascading2() {
        context.putProperty("p", "HELLO");
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading2.groovy")
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT");
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.getLayout().pattern)
    }


    @Test
    @Ignore
    void receiver() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "propertyCascading2.groovy")
        configurator.run file
    }

    @Test
    void appenderRefShouldWork() {
        File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "asyncAppender.groovy")
        configurator.run file

        def aa = (AsyncAppender) root.getAppender('STDOUT-ASYNC');
        assertTrue aa.isStarted()
        def stdout = (ConsoleAppender) aa.getAppender('STDOUT')
        assertNotNull stdout
    }

    @Test
    void appenderRefWithNonAppenderAttachable() {
        String message = shouldFail(IllegalArgumentException) {
            File file = new File(ClassicTestConstants.GAFFER_INPUT_PREFIX + "appenderRefWithNonAppenderAttachable.groovy")
            configurator.run file
        }
        assertEquals message, "ch.qos.logback.core.ConsoleAppender does not implement ch.qos.logback.core.spi.AppenderAttachable."
    }

    final static String INCLUDE_KEY = "includeKey"
    final static String SUB_FILE_KEY = "subFileKey"
	final static String FIRST_FILE_KEY = "firstFileKey"
    final static String SECOND_FILE_KEY = "secondFileKey"
    final static String INCLUSION_DIR_PREFIX = ClassicTestConstants.GAFFER_INPUT_PREFIX + "inclusion/"
    final static String TOP_BY_FILE = INCLUSION_DIR_PREFIX + "topByFile.groovy"
    final static String TOP_BY_URL = INCLUSION_DIR_PREFIX + "topByUrl.groovy"
    final static String TOP_BY_STRING = INCLUSION_DIR_PREFIX + "topByString.groovy"
    final static String TOP_OPTIONAL = INCLUSION_DIR_PREFIX + "topOptional.groovy"
    final static String INTERMEDIARY_FILE = INCLUSION_DIR_PREFIX + "intermediaryByFile.groovy"
    final static String SUB_FILE = INCLUSION_DIR_PREFIX + "subByFile.groovy"
    final static String MULTI_INCLUDE_BY_FILE = INCLUSION_DIR_PREFIX + "multiIncludeByFile.groovy"
    final static String FIRST_FILE = INCLUSION_DIR_PREFIX + "first.groovy"
    final static String SECOND_FILE = INCLUSION_DIR_PREFIX + "second.groovy"
    final static String INCLUDED_FILE = INCLUSION_DIR_PREFIX + "included.groovy"
    final static String URL_TO_INCLUDE = "file:./" + INCLUDED_FILE
    final static String INVALID = INCLUSION_DIR_PREFIX + "invalid.groovy"
    final static String MISSINGMETHOD = INCLUSION_DIR_PREFIX + "missingmethod.groovy"
    final static String INCLUDED_AS_RESOURCE = "asResource/gaffer/inclusion/includedAsResource.groovy"

    @Test
    void includeBasicFile() {
        System.setProperty(INCLUDE_KEY, INCLUDED_FILE)
        File file = new File(TOP_BY_FILE)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void includeBasicURL() {
        System.setProperty(INCLUDE_KEY, URL_TO_INCLUDE)
        File file = new File(TOP_BY_URL)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void includeBasicURLAsString() {
        System.setProperty(INCLUDE_KEY, URL_TO_INCLUDE);
        File file = new File(TOP_BY_STRING)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

	@Test
    void includeBasicResourceAsString() {
        System.setProperty(INCLUDE_KEY, INCLUDED_AS_RESOURCE)
        File file = new File(TOP_BY_STRING)
        configurator.run file

		StatusPrinter.print context
        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void includeBasicFileAsString() {
        System.setProperty(INCLUDE_KEY, INCLUDED_FILE)
        File file = new File(TOP_BY_STRING)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

	@Test
    void includeOptionalFile() {
        File file = new File(TOP_OPTIONAL)
        configurator.run file
        StatusPrinter.print context
        assertEquals(Status.INFO, statusChecker.getHighestLevel(0))
    }

    @Test
    void includeNoFileFound() {
        System.setProperty(INCLUDE_KEY, "toto")
        File file = new File(TOP_BY_FILE)
        configurator.run file
        StatusPrinter.print context
        assertEquals(Status.WARN, statusChecker.getHighestLevel(0))
    }

    @Test
    void includeUnknownURL() {
        System.setProperty(INCLUDE_KEY, "http://logback2345.qos.ch")
        File file = new File(TOP_BY_URL)
        configurator.run file
        StatusPrinter.print context
        assertEquals(Status.WARN, statusChecker.getHighestLevel(0))
    }

    @Test(expected=CompilationFailedException.class)
    void includeInvalidScript() {
        System.setProperty(INCLUDE_KEY, INVALID)
        File file = new File(TOP_BY_FILE)
        configurator.run file
    }

    @Test(expected=MissingMethodException.class)
    void includeScriptWithMisingMethod() {
        System.setProperty(INCLUDE_KEY, MISSINGMETHOD)
        File file = new File(TOP_BY_FILE)
        configurator.run file
    }

    @Test
    void nestedInclude() {
		System.setProperty(SUB_FILE_KEY, SUB_FILE)
        System.setProperty(INCLUDE_KEY, INTERMEDIARY_FILE)
        File file = new File(TOP_BY_FILE)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
		assertNotNull(ca)
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

    @Test
    void multiInclude() {
        System.setProperty(FIRST_FILE_KEY, FIRST_FILE)
        System.setProperty(SECOND_FILE_KEY, SECOND_FILE)
        File file = new File(MULTI_INCLUDE_BY_FILE)
        configurator.run file

        ConsoleAppender ca = (ConsoleAppender) root.getAppender("STDOUT")
		assertNotNull(ca)
        assertTrue ca.isStarted()
        assertEquals("HELLO %m%n", ca.encoder.layout.pattern)
    }

}
