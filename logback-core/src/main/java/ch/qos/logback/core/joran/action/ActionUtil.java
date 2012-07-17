package ch.qos.logback.core.joran.action;

import java.util.Properties;

import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.ContextUtil;
import ch.qos.logback.core.util.OptionHelper;

public class ActionUtil {

  public enum Scope {
    LOCAL, CONTEXT, SYSTEM
  };

  static public Scope stringToScope(String scopeStr) {
    if(Scope.SYSTEM.toString().equalsIgnoreCase(scopeStr))
      return Scope.SYSTEM;
     if(Scope.CONTEXT.toString().equalsIgnoreCase(scopeStr))
      return Scope.CONTEXT;

    return Scope.LOCAL;
  }

  static public void setProperty(InterpretationContext ic, String key, String value, Scope scope) {
    switch (scope) {
    case LOCAL:
      ic.addSubstitutionProperty(key, value);
      break;
    case CONTEXT:
      ic.getContext().putProperty(key, value);
      break;
    case SYSTEM:
      OptionHelper.setSystemProperty(ic, key, value);
    }
  }

  /**
   * Add all the properties found in the argument named 'props' to an
   * InterpretationContext.
   */
  static public void setProperties(InterpretationContext ic, Properties props,
      Scope scope) {
    switch (scope) {
    case LOCAL:
      ic.addSubstitutionProperties(props);
      break;
    case CONTEXT:
      ContextUtil cu = new ContextUtil(ic.getContext());
      cu.addProperties(props);
      break;
    case SYSTEM:
      OptionHelper.setSystemProperties(ic, props);
    }
  }

}
