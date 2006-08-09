package ch.qos.logback.classic.joran.action;

import ch.qos.logback.classic.ClassicLayout;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.action.AbstractLayoutAction;


public class LayoutAction extends AbstractLayoutAction {

  protected boolean isOfCorrectType(Layout layout) {
    return (layout instanceof ClassicLayout);
  }

}
