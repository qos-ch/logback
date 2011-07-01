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
