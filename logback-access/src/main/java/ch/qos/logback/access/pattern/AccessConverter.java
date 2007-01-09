package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.Status;


abstract public class AccessConverter extends DynamicConverter<AccessEvent> implements ContextAware {

  public final static char SPACE_CHAR = ' ';
  public final static char QUESTION_CHAR = '?';
  
  ContextAwareBase cab = new ContextAwareBase();
  
  public void setContext(Context context) {
   cab.setContext(context);
  }

  public Context getContext() {
    return cab.getContext();
  }
  
  public void addStatus(Status status) {
    cab.addStatus(status);
  }

  public void addInfo(String msg) {
    cab.addInfo(msg);
  }

  public void addInfo(String msg, Throwable ex) {
    cab.addInfo(msg, ex);
  }

  public void addWarn(String msg) {
    cab.addWarn(msg);
  }

  public void addWarn(String msg, Throwable ex) {
    cab.addWarn(msg, ex);
  }

  public void addError(String msg) {
    addError(msg);
  }

  public void addError(String msg, Throwable ex) {
    addError(msg, ex);
  }
  
}
