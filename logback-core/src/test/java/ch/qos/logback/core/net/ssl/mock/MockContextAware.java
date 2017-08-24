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
package ch.qos.logback.core.net.ssl.mock;

import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A {@link ContextAware} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockContextAware extends ContextAwareBase implements ContextAware {

    private final List<String> info = new LinkedList<String>();
    private final List<String> warn = new LinkedList<String>();
    private final List<String> error = new LinkedList<String>();

    @Override
    public void addInfo(String msg) {
        info.add(msg);
    }

    @Override
    public void addWarn(String msg) {
        warn.add(msg);
    }

    @Override
    public void addError(String msg) {
        error.add(msg);
    }

    public boolean hasInfoMatching(String regex) {
        return hasMatching(info, regex);
    }

    public boolean hasWarnMatching(String regex) {
        return hasMatching(info, regex);
    }

    public boolean hasErrorMatching(String regex) {
        return hasMatching(info, regex);
    }

    private boolean hasMatching(List<String> messages, String regex) {
        for (String message : messages) {
            if (message.matches(regex))
                return true;
        }
        return false;
    }

}
