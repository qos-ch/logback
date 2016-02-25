/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Collect all incoming events in a list.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class StatusListenerAsList implements StatusListener {

    List<Status> statusList = new ArrayList<Status>();

    public void addStatusEvent(Status status) {
        statusList.add(status);
    }

    public List<Status> getStatusList() {
        return statusList;
    }

}
