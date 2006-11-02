package ch.qos.logback.core.filter;

import ch.qos.logback.core.spi.FilterReply;

/**
 * <p>
 * This abstract class is meant to be a base for specific evaluator filters.
 * </p>
 * <p>
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
 * @author Ceki G&uuml;lc&uuml;
 *
 */

public abstract class AbstractEvalutatorFilter extends Filter {

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
}
