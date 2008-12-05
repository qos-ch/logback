/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.status;

import ch.qos.logback.core.Context;

public class StatusUtil {

  static public void addStatus(Context context, Status status) {
    if (context == null) {
      return;
    }
    StatusManager sm = context.getStatusManager();
    if (sm != null) {
      sm.add(status);
    }
  }
 
  static public void addInfo(Context context, Object caller, String msg) {
    addStatus(context, new InfoStatus(msg, caller));
  }

  static public void addWarn(Context context, Object caller, String msg) {
    addStatus(context, new WarnStatus(msg, caller));
  }
  
  static public void addError(Context context, Object caller, String msg,
      Throwable t) {
    addStatus(context, new ErrorStatus(msg, caller, t));
  }
}
