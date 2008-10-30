/**
 * LOGBack: the generic, reliable, fast and flexible logging framework.
 *
 * Copyright (C) 1999-2006, QOS.ch
 *
 * This library is free software, you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 */
package ch.qos.logback.classic.joran;

import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.boolex.JaninoEventEvaluator;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.TeztConstants;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.joran.spi.JoranException;


public class EvaluatorJoranTest extends TestCase {

  public void xtest() throws NullPointerException, EvaluationException, JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(TeztConstants.TEST_DIR_PREFIX + "input/joran/simpleEvaluator.xml");
    
    
    Map evalMap = (Map) loggerContext.getObject(CoreConstants.EVALUATOR_MAP);
    assertNotNull(evalMap);
    //StatusPrinter.print(loggerContext.getStatusManager());
    JaninoEventEvaluator evaluator = (JaninoEventEvaluator) evalMap.get("msgEval");
    assertNotNull(evaluator);
    
    Logger logger = loggerContext.getLogger("xx");
    LoggingEvent event = new LoggingEvent("foo", logger, Level.DEBUG, "Hello world", null, null);
    //StatusPrinter.print(loggerContext.getStatusManager());
    assertTrue(evaluator.evaluate(event));
    
    //StatusPrinter.print(loggerContext.getStatusManager());
  }
  
  public void testIgnoreMarker() throws NullPointerException, EvaluationException, JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    LoggerContext loggerContext = new LoggerContext();
    jc.setContext(loggerContext);
    jc.doConfigure(TeztConstants.TEST_DIR_PREFIX + "input/joran/ignore.xml");
    
    //StatusPrinter.print(loggerContext.getStatusManager());
    
    Map evalMap = (Map) loggerContext.getObject(CoreConstants.EVALUATOR_MAP);
    assertNotNull(evalMap);
    //StatusPrinter.print(loggerContext.getStatusManager());
    
    Logger logger = loggerContext.getLogger("xx");
    
    JaninoEventEvaluator evaluator = (JaninoEventEvaluator) evalMap.get("IGNORE_EVAL");
    LoggingEvent event = new LoggingEvent("foo", logger, Level.DEBUG, "Hello world",null, null);
    //StatusPrinter.print(loggerContext.getStatusManager());
    
    Marker ignoreMarker = MarkerFactory.getMarker("IGNORE");
    event.setMarker(ignoreMarker);
    assertTrue(evaluator.evaluate(event));
    
    logger.debug("hello", new Exception("test"));
    logger.debug(ignoreMarker, "hello ignore", new Exception("test"));
    
    //logger.debug("hello", new Exception("test"));
    
    //StatusPrinter.print(loggerContext.getStatusManager());
  }
  
  public void testMultipleConditionsInExpression() throws NullPointerException, EvaluationException {
    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger("xx");
    JaninoEventEvaluator ee = new JaninoEventEvaluator();
    ee.setName("testEval");
    ee.setContext(loggerContext);
    //&#38;&#38;
    //&amp;&amp;
    ee.setExpression("message.contains(\"stacktrace\") && message.contains(\"logging\")");
    ee.start();
    //StatusPrinter.print(loggerContext);
    
    String message = "stacktrace bla bla logging";
    LoggingEvent event = new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, message, null, null);
    
    assertTrue(ee.evaluate(event));
  }
}
