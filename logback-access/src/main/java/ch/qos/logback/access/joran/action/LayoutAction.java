package ch.qos.logback.access.joran.action;

import ch.qos.logback.access.AccessLayout;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.action.AbstractLayoutAction;


public class LayoutAction extends AbstractLayoutAction {

  protected boolean isOfCorrectType(Layout layout) {
    return (layout instanceof AccessLayout);
  }

}
