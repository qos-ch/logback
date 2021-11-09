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

import java.util.Iterator;

public interface Status {

    int INFO = 0;
    int WARN = 1;
    int ERROR = 2;

    int getLevel();

    int getEffectiveLevel();

    Object getOrigin();

    String getMessage();

    Throwable getThrowable();

    Long getDate();

    boolean hasChildren();

    void add(Status child);

    boolean remove(Status child);

    Iterator<Status> iterator();

}
