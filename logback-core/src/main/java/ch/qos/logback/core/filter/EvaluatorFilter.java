package ch.qos.logback.core.filter;

import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The value of the {@link #onMatch} and {@link #onMismatch} attributes is set to 
 * {@link Filter.NEUTRAL}, so that a badly configured evaluator filter doesn't 
 * disturb the functionning of the chain.
 * </p>
 * <p>
 * It is expected that one of the two attributes will have its value changed to
 * {@link Filter.ACCEPT} or {@link Filter.DENY}. That way, it is possible to decide if 
 * a given result must be returned after the evaluation either failed or succeeded.
 * </p>
 * 
 * For more informations about filters, please refer to the online manual at
 * http://logback.qos.ch/manual/filters.html
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class EvaluatorFilter extends Filter {

  EventEvaluator evaluator;
  
  protected FilterReply onMatch = FilterReply.NEUTRAL;
  protected FilterReply onMismatch = FilterReply.NEUTRAL;

  final public void setOnMatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMatch = FilterReply.DENY;
    }
  }

  final public void setOnMismatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMismatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMismatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMismatch = FilterReply.DENY;
    }
  }
  
  @Override
  public void start() {
    if(evaluator != null) {
      super.start();
    } else {
      addError("No evaluator set for filter "+this.getName());
    }
  }
  
  public EventEvaluator getEvaluator() {
    return evaluator;
  }

                 
  public void setEvaluator(EventEvaluator evaluator) {
    this.evaluator = evaluator;
  }

  public FilterReply decide(Object event) {
    // let us not throw an exception
    // see also bug #17.
    if(!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    try {
      if (evaluator.evaluate(event)) {
        return onMatch;
      } else {
        return onMismatch;
      }
    } catch (EvaluationException e) {
      addError("Evaluator "+evaluator.getName()+" threw an exception", e);
      return FilterReply.NEUTRAL;
    }
  }

}
