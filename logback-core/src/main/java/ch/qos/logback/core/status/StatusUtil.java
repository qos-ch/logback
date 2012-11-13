/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.status;

import ch.qos.logback.core.Context;

import java.util.ArrayList;
import java.util.List;

public class StatusUtil {

  /**
   * Returns true if the StatusManager associated with the context passed
   * as parameter has one or more StatusListener instances registered. Returns
   * false otherwise.
   *
   * @param context
   * @return true if one or more StatusListeners registered, false otherwise
   * @since 1.0.8
   */
  static public boolean contextHasStatusListener(Context context) {
    StatusManager sm = context.getStatusManager();
    if(sm == null)
      return false;
    List<StatusListener> listeners = sm.getCopyOfStatusListenerList();
    if(listeners == null || listeners.size() == 0)
      return false;
    else
      return true;
  }

  static public List<Status> filterStatusListByTimeThreshold(List<Status> rawList, long threshold) {
    List<Status> filteredList = new ArrayList<Status>();
    for (Status s : rawList) {
      if (s.getDate() >= threshold)
        filteredList.add(s);
    }
    return filteredList;
  }

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
