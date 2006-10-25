package ch.qos.logback.core.filter;

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

abstract public class AbstractEvalutatorFilter extends Filter {

  int onMatch = NEUTRAL;
  int onMismatch = NEUTRAL;

                 
  public void setOnMatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMatch = NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMatch = ACCEPT;
    } else if ("DENY".equals(action)) {
      onMatch = DENY;
    }
  }

  public void setOnMismatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMismatch = NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMismatch = ACCEPT;
    } else if ("DENY".equals(action)) {
      onMismatch = DENY;
    }
  }

  public int getOnMatch() {
    return onMatch;
  }

  public int getOnMistmatch() {
    return onMismatch;
  }
}
