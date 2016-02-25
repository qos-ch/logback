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
package ch.qos.logback.access.pattern;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.Status;

abstract public class AccessConverter extends DynamicConverter<IAccessEvent> implements ContextAware {

    public final static char SPACE_CHAR = ' ';
    public final static char QUESTION_CHAR = '?';

    ContextAwareBase cab = new ContextAwareBase();

    @Override
    public void setContext(Context context) {
        cab.setContext(context);
    }

    @Override
    public Context getContext() {
        return cab.getContext();
    }

    @Override
    public void addStatus(Status status) {
        cab.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        cab.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        cab.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        cab.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        cab.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        cab.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        cab.addError(msg, ex);
    }

}
