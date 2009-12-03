package ch.qos.logback.reflect;

import org.codehaus.janino.ExpressionEvaluator;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.codehaus.janino.samples.DemoBase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;


public class JaninoTest {

  /**
   * @param args
   * @throws ScanException
   * @throws ParseException
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    String expression = "import ch.qos.logback.classic.Level; e.getLevel().levelInt <= Level.INFO.levelInt";
    Class optionalExpressionType = boolean.class;
    String[] parameterNames = {"e"};
    Class[] parameterTypes = {LoggingEvent.class};
    Class[] thrownExceptions = new Class[0];

    // Create "ExpressionEvaluator" object.
    ExpressionEvaluator ee = new ExpressionEvaluator(expression,
        optionalExpressionType, parameterNames, parameterTypes,
        thrownExceptions, null // optionalClassLoader
    );

    Object[] parameterValues = new Object[1];
    LoggerContext lc = new LoggerContext();
    
    Logger logger = lc.getLogger(JaninoTest.class);
    LoggingEvent loggingEvent = new LoggingEvent("toto", logger, Level.INFO, "hi", null);
    parameterValues[0] = loggingEvent;
    
    Object res = ee.evaluate(parameterValues);

    // Print expression result.
    System.out.println("Result = " + DemoBase.toString(res));
    System.out.println("Type = " + res.getClass().getName());
    loop(ee, parameterValues);

  //   loggingEvent.getMarker()
    
    expression = "import ch.qos.logback.classic.Level; (e.getMarker() != null) && (e.getMarker().contains(\"yo\"))";
    ee = new ExpressionEvaluator(expression,
        optionalExpressionType, parameterNames, parameterTypes,
        thrownExceptions, null);
    res = ee.evaluate(parameterValues);

    // Print expression result.
    System.out.println("Result = " + DemoBase.toString(res));
    System.out.println("Type = " + res.getClass().getName());
    loop(ee, parameterValues);
    
  }

  static void loop(ExpressionEvaluator ee, Object[] parameterValues)
      throws Exception {
    final long start = System.nanoTime();
    final long LEN = 1000 * 1000;
    for (int i = 0; i < LEN; i++) {
      ee.evaluate(parameterValues);
    }
    final long end = System.nanoTime();
    System.out.println("abg: " + (end - start) / LEN + " nanos");

  }

}
