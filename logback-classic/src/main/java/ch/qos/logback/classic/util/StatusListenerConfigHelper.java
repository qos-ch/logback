package ch.qos.logback.classic.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.OptionHelper;

public class StatusListenerConfigHelper {

  final public static String STATUS_LISTENER_CLASS = "logback.statusListenerClass";
  final public static String SYSOUT = "SYSOUT";

  
  static void installIfAsked(LoggerContext loggerContext) {
    String slClass = System.getProperty(STATUS_LISTENER_CLASS, null);
    if (!OptionHelper.isEmpty(slClass)) {
      addStatusListener(loggerContext, slClass);
    } 
  }
  
  static void addStatusListener(LoggerContext loggerContext,
      String listenerClass) {
    StatusListener listener = null;
    if (SYSOUT.equalsIgnoreCase(listenerClass)) {
      listener = new OnConsoleStatusListener();
    } else {
      try {
        listener = (StatusListener) OptionHelper.instantiateByClassName(
            listenerClass, StatusListener.class, loggerContext);
        loggerContext.getStatusManager().add(listener);
      } catch (Exception e) {
        // printing on the console is the best we can do
        e.printStackTrace();
      }
    }
    if (listener != null) {
      loggerContext.getStatusManager().add(listener);
    }
  }
}
