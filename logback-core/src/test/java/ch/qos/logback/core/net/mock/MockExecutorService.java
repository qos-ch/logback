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
package ch.qos.logback.core.net.mock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An {@link ExecutorService} with instrumentation for unit testing.
 * <p>
 * This service is synchronous; submitted jobs are run on the calling thread.
 *
 * @author Carl Harris
 */
public class MockExecutorService extends AbstractExecutorService {

    private Runnable lastCommand;

    public Runnable getLastCommand() {
        return lastCommand;
    }

    public void shutdown() {
    }

    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    public boolean isShutdown() {
        return true;
    }

    public boolean isTerminated() {
        return true;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }

    public void execute(Runnable command) {
        command.run();
        lastCommand = command;
    }

}