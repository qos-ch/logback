package ch.qos.logback.core.boolex;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.janino.ExpressionEvaluator;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

abstract public class JaninoEventEvaluatorBase extends ContextAwareBase
    implements EventEvaluator, LifeCycle {

  static Class EXPRESSION_TYPE = boolean.class;
  static Class[] THROWN_EXCEPTIONS = new Class[1];

  static public final int ERROR_THRESHOLD = 4;
  static {
    THROWN_EXCEPTIONS[0] = EvaluationException.class;
  }
  
  
  protected boolean start = false;

  private String name;
  private String expression;

  ExpressionEvaluator ee;
  private int errorCount = 0;

  abstract protected String getDecoratedExpression();

  abstract protected String[] getParameterNames();

  abstract protected Class[] getParameterTypes();

  abstract protected Object[] getParameterValues(Object event);

  protected List<Matcher> matcherList = new ArrayList<Matcher>();

  public boolean isStarted() {
    return start;
  }

  public void stop() {
    start = false;
  }

  public void start() {
    try {
      assert context != null;
      ClassLoader cl = context.getClass().getClassLoader();
      ee = new ExpressionEvaluator(getDecoratedExpression(), EXPRESSION_TYPE,
          getParameterNames(), getParameterTypes(), THROWN_EXCEPTIONS, cl);
      start = true;
    } catch (Exception e) {
      addError(
          "Could not start evaluator with expression [" + expression + "]", e);
    }
  }

  public boolean evaluate(Object event) throws EvaluationException {
    if (!start) {
      throw new IllegalStateException("Evaluator [" + name + "] was called in stopped state");
    }
    try {
      Boolean result = (Boolean) ee.evaluate(getParameterValues(event));
      return result.booleanValue();
    } catch (Exception ex) {
      errorCount++;
      if (errorCount >= ERROR_THRESHOLD) {
        start = false;
      }
      throw new EvaluationException("Evaluator [" + name
          + "] caused an exception", ex);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalStateException("name has been already set");
    }
    this.name = name;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public void addMatcher(Matcher matcher) {
    matcherList.add(matcher);
  }

  public List getMatcherList() {
    return matcherList;
  }
}
