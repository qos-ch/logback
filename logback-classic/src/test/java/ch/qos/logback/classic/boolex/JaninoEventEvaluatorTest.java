package ch.qos.logback.classic.boolex;

import junit.framework.TestCase;

import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ConverterTest;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.JaninoEventEvaluatorBase;
import ch.qos.logback.core.boolex.Matcher;

public class JaninoEventEvaluatorTest extends TestCase {

  LoggerContext loggerContext = new LoggerContext();
  Logger logger = loggerContext.getLogger(ConverterTest.class);

  Matcher matcherX = new Matcher();

  public JaninoEventEvaluatorTest(String arg0) {
    super(arg0);

    matcherX.setName("x");
    matcherX.setRegex("^Some\\s.*");
    matcherX.start();

  }

  LoggingEvent makeLoggingEvent(Exception ex) {
    LoggingEvent e = new LoggingEvent(
        ch.qos.logback.core.pattern.FormattingConverter.class.getName(),
        logger, Level.INFO, "Some message", ex, null);
    return e;
  }

  public void testBasic() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("message.equals(\"Some message\")");
    jee.setContext(loggerContext);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    System.out.println(event);
    assertTrue(jee.evaluate(event));
  }

  public void testLevel() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("level > DEBUG");
    jee.setContext(loggerContext);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    System.out.println(event);
    assertTrue(jee.evaluate(event));
  }

  public void testtimeStamp() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("timeStamp > 10");
    jee.setContext(loggerContext);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    assertTrue(jee.evaluate(event));
  }

  public void testWithMatcher() throws Exception {

    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("x.matches(message)");
    jee.setContext(loggerContext);
    jee.addMatcher(matcherX);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    assertTrue(jee.evaluate(event));
  }

  public void testMarker() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("marker.contains(\"BLUE\")");
    jee.setContext(loggerContext);
    jee.addMatcher(matcherX);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    event.setMarker(MarkerFactory.getMarker("BLUE"));
    assertTrue(jee.evaluate(event));
  }

  public void testWithNullMarker() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("marker.contains(\"BLUE\")");
    jee.setContext(loggerContext);
    jee.addMatcher(matcherX);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    try {
      jee.evaluate(event);
      fail("We should not reach this point");
    } catch (EvaluationException ee) {

    }
  }

  public void testComplex() throws Exception {

    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee
        .setExpression("level >= INFO && x.matches(message) && marker.contains(\"BLUE\")");
    jee.setContext(loggerContext);
    jee.addMatcher(matcherX);
    jee.start();

    LoggingEvent event = makeLoggingEvent(null);
    event.setMarker(MarkerFactory.getMarker("BLUE"));
    assertTrue(jee.evaluate(event));
  }

  /**
   * check that evaluator with bogis exp does not start
   * 
   * @throws Exception
   */
  public void testBogusExp1() {

    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("mzzzz.get(\"key\").equals(null)");
    jee.setContext(loggerContext);
    jee.setName("bogus");
    jee.start();

    assertFalse(jee.isStarted());

    // StatusPrinter.print(loggerContext);
    // LoggingEvent event = makeLoggingEvent(null);
    // event.setMarker(MarkerFactory.getMarker("BLUE"));
    //    
    // jee.evaluate(event);
  }

  // check that eval stops after errors
  public void testBogusExp2() {

    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("mdc.get(\"key\").equals(null)");
    jee.setContext(loggerContext);
    jee.setName("bogus");
    jee.start();

    assertTrue(jee.isStarted());

    LoggingEvent event = makeLoggingEvent(null);

    for (int i = 0; i < JaninoEventEvaluatorBase.ERROR_THRESHOLD; i++) {
      try {
        jee.evaluate(event);
        fail("should throw an exception");
      } catch (EvaluationException e) {
      }
    }
    // after a few attempts the evaluator should stop
    assertFalse(jee.isStarted());

  }

  static final long LEN = 10 * 1000;

  // with 6 parameters 400 nanos
  // with 7 parameters 460 nanos (all levels + selected fields from
  // LoggingEvent)
  // with 10 parameters 510 nanos (all levels + fields)
  void loop(JaninoEventEvaluator jee, String msg) throws Exception {
    LoggingEvent event = makeLoggingEvent(null);
    final long start = System.nanoTime();
    for (int i = 0; i < LEN; i++) {
      jee.evaluate(event);
    }
    final long end = System.nanoTime();
    System.out.println(msg + (end - start) / LEN + " nanos");
  }

  public void testLoop1() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("timeStamp > 10");
    jee.setContext(loggerContext);
    jee.start();

    loop(jee, "timestamp > 10]: ");
  }

  public void testLoop2() throws Exception {
    JaninoEventEvaluator jee = new JaninoEventEvaluator();
    jee.setExpression("x.matches(message)");
    jee.setContext(loggerContext);
    jee.addMatcher(matcherX);
    jee.start();

    loop(jee, "x.matches(message): ");
  }

}
