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
import java.util.Iterator;
import java.util.List;

abstract public class StatusBase implements Status {

    static private final List<Status> EMPTY_LIST = new ArrayList<Status>(0);

    int level;
    final String message;
    final Object origin;
    List<Status> childrenList;
    Throwable throwable;
    long date;

    StatusBase(int level, String msg, Object origin) {
        this(level, msg, origin, null);
    }

    StatusBase(int level, String msg, Object origin, Throwable t) {
        this.level = level;
        this.message = msg;
        this.origin = origin;
        this.throwable = t;
        this.date = System.currentTimeMillis();
    }

    public synchronized void add(Status child) {
        if (child == null) {
            throw new NullPointerException("Null values are not valid Status.");
        }
        if (childrenList == null) {
            childrenList = new ArrayList<Status>();
        }
        childrenList.add(child);
    }

    public synchronized boolean hasChildren() {
        return ((childrenList != null) && (childrenList.size() > 0));
    }

    public synchronized Iterator<Status> iterator() {
        if (childrenList != null) {
            return childrenList.iterator();
        } else {
            return EMPTY_LIST.iterator();
        }
    }

    public synchronized boolean remove(Status statusToRemove) {
        if (childrenList == null) {
            return false;
        }
        // TODO also search in childrens' children
        return childrenList.remove(statusToRemove);
    }

    public int getLevel() {
        return level;
    }

    // status messages are not supposed to contains cycles.
    // cyclic status arrangements are like to cause deadlocks
    // when this method is called from different thread on
    // different status objects lying on the same cycle
    public synchronized int getEffectiveLevel() {
        int result = level;
        int effLevel;

        Iterator<Status> it = iterator();
        Status s;
        while (it.hasNext()) {
            s = (Status) it.next();
            effLevel = s.getEffectiveLevel();
            if (effLevel > result) {
                result = effLevel;
            }
        }
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Object getOrigin() {
        return origin;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Long getDate() {
        return date;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        switch (getEffectiveLevel()) {
        case INFO:
            buf.append("INFO");
            break;
        case WARN:
            buf.append("WARN");
            break;
        case ERROR:
            buf.append("ERROR");
            break;
        }
        if (origin != null) {
            buf.append(" in ");
            buf.append(origin);
            buf.append(" -");
        }

        buf.append(" ");
        buf.append(message);

        if (throwable != null) {
            buf.append(" ");
            buf.append(throwable);
        }

        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StatusBase other = (StatusBase) obj;
        if (level != other.level)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

}
