package ch.qos.logback.access.net;

import ch.qos.logback.access.boolex.JaninoEventEvaluator;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.boolex.EvaluationException;

public class DefaultSMTPEvaluator extends JaninoEventEvaluator {
  
  private static final long ONE_DAY = 1000*60*60*24;
  private long LAST_TRIGGER_DATE = 0L;
  
  public DefaultSMTPEvaluator(Context context) {
    this.context = context;
    setName("SMTPAppender's default event evaluator");
    setExpression("event.getStatusCode() >= 500");
  }
 
  /**
   * Is this <code>event</code> the e-mail triggering event?
   * 
   * <p>
   * This method returns <code>true</code>, if the event is
   * evaluated to true. Otherwise it returns <code>false</code>.
   * 
   * Once an email is sent, the next one will not be sent unless a certain amount
   * of time passed.
   */
  @Override
  public boolean evaluate(Object event) throws EvaluationException {
    
    if (super.evaluate(event)) {
      if (System.currentTimeMillis() >= LAST_TRIGGER_DATE + ONE_DAY) {
        LAST_TRIGGER_DATE = System.currentTimeMillis();
        return true;
      } 
    }
    return false;
  }
}
