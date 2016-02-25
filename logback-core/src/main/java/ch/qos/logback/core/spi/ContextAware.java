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
package ch.qos.logback.core.spi;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.Status;

/**
 * An object which has a context and add methods for updating internal status messages.
 */
public interface ContextAware {

    void setContext(Context context);

    Context getContext();

    void addStatus(Status status);

    void addInfo(String msg);

    void addInfo(String msg, Throwable ex);

    void addWarn(String msg);

    void addWarn(String msg, Throwable ex);

    void addError(String msg);

    void addError(String msg, Throwable ex);

}
