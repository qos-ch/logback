/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.core.testUtil.RunnableWithCounterAndDone;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

class UpdaterRunnable extends RunnableWithCounterAndDone {
    private final ReconfigureOnChangeTaskTest reconfigureOnChangeTaskTest;
    File configFile;
    ReconfigureOnChangeTaskTest.UpdateType updateType;

    // it actually takes time for Windows to propagate file modification changes
    // values below 100 milliseconds can be problematic the same propagation
    // latency occurs in Linux but is even larger (>600 ms)
    // final static int DEFAULT_SLEEP_BETWEEN_UPDATES = 60;

    final int sleepBetweenUpdates = 100;

    UpdaterRunnable(ReconfigureOnChangeTaskTest reconfigureOnChangeTaskTest, File configFile, ReconfigureOnChangeTaskTest.UpdateType updateType) {
        this.reconfigureOnChangeTaskTest = reconfigureOnChangeTaskTest;
        this.configFile = configFile;
        this.updateType = updateType;
    }

    UpdaterRunnable(ReconfigureOnChangeTaskTest reconfigureOnChangeTaskTest, File configFile) {
        this(reconfigureOnChangeTaskTest, configFile, ReconfigureOnChangeTaskTest.UpdateType.TOUCH);
    }

    public void run() {
        while (!isDone()) {
            try {
                Thread.sleep(sleepBetweenUpdates);
            } catch (InterruptedException e) {
            }
            if (isDone()) {
                reconfigureOnChangeTaskTest.addInfo("Exiting Updater.run()", this);
                return;
            }
            counter++;
            reconfigureOnChangeTaskTest.addInfo("Touching [" + configFile + "]", this);
            switch (updateType) {
            case TOUCH:
                touchFile();
                break;
            case MALFORMED:
                try {
                    malformedUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("malformedUpdate failed");
                }
                break;
            case MALFORMED_INNER:
                try {
                    malformedInnerUpdate();
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("malformedInnerUpdate failed");
                }
            }
        }
        reconfigureOnChangeTaskTest.addInfo("Exiting Updater.run()", this);
    }

    private void malformedUpdate() throws IOException {
        reconfigureOnChangeTaskTest.writeToFile(configFile,
                        "<configuration scan=\"true\" scanPeriod=\"50 millisecond\">\n" + "  <root level=\"ERROR\">\n" + "</configuration>");
    }

    private void malformedInnerUpdate() throws IOException {
        reconfigureOnChangeTaskTest.writeToFile(configFile, "<included>\n" + "  <root>\n" + "</included>");
    }

    void touchFile() {

        boolean result = configFile.setLastModified(System.currentTimeMillis());
        if (!result)
            reconfigureOnChangeTaskTest.addWarn(this.getClass().getName() + ".touchFile on " + configFile.toString() + " FAILED", this);
    }
}
