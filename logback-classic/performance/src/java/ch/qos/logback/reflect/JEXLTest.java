package ch.qos.logback.reflect;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;


public class JEXLTest {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {

    LoggerContext lc = new LoggerContext();
    Logger logger = lc.getLogger(JaninoTest.class);
    LoggingEvent loggingEvent = new LoggingEvent("toto", logger, Level.INFO,
        "hi", null);

    // Create an expression object
    String jexlExp = "e.message == 'hix'";
    Expression e = ExpressionFactory.createExpression(jexlExp);

    // Create a context and add data
    JexlContext jc = JexlHelper.createContext();
    jc.getVars().put("e", loggingEvent);

    // Now evaluate the expression, getting the result
    Object o = e.evaluate(jc);
    System.out.println("==" + o);
    
    //loop(e, jc);
    
    findClassLoop("java.lang.Exception", "java.net.UnknownHostException");
  }

  static void loop(Expression e, JexlContext jc)
      throws Exception {
    final long start = System.nanoTime();
    final long LEN = 1000 * 1000;
    for (int i = 0; i < LEN; i++) {
      e.evaluate(jc);
    }
    final long end = System.nanoTime();
    System.out.println("JEXL avg: " + (end - start) / LEN + " nanos");
  }
  
  static void findClassLoop(String superClassStr, String childClassStr) throws ClassNotFoundException {
    final long start = System.nanoTime();
    final long LEN = 1000 * 1000;
    Class superClass = Class.forName(superClassStr);
    
    for (int i = 0; i < LEN; i++) {
      Class childClass = Class.forName(childClassStr);
      superClass.isAssignableFrom(childClass);
    }
    final long end = System.nanoTime();
    System.out.println("FIND CLA avg: " + (end - start) / LEN + " nanos");
  }
}
