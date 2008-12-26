package ch.qos.logback.access.joran.action;

import ch.qos.logback.access.boolex.JaninoEventEvaluator;
import ch.qos.logback.core.joran.action.AbstractEventEvaluatorAction;


public class EvaluatorAction extends AbstractEventEvaluatorAction {

  protected String defaultClassName() {
    return JaninoEventEvaluator.class.getName();
  }
}
