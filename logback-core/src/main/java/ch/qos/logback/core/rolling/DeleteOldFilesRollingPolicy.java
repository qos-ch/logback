/*
 * Copyright (c) 2014 QOS.ch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QOS.ch - initial API and implementation and/or initial documentation
 */
package ch.qos.logback.core.rolling;

import java.io.File;

/**
 *
 *
 */
public class DeleteOldFilesRollingPolicy extends RollingPolicyBase {

    /**
     * How many days to keep a file.
     */
    protected long daysToKeep;

    public void rollover() throws RolloverFailure {
        File logFile = new File(".", getActiveFileName());
        File logFileDir = logFile.getParentFile();

        if (logFileDir.exists() == false) {
            addInfo("Skipping non existing dir: " + logFileDir.getAbsolutePath());
            return;
        }

        long now = System.currentTimeMillis();

        long filesDeleted = 0;

        for (File f : logFileDir.listFiles()) {
            long lastModified = f.lastModified();

            long ageInMilliSeconds = now - lastModified;
            long ageInSeconds = ageInMilliSeconds / 1000;
            long ageInHours = ageInSeconds / 3600;
            long ageInDays = ageInHours / 24;

            final String absolutePath = f.getAbsolutePath();

            if (ageInDays > daysToKeep) {
                if (f.delete()) {
                    addInfo("Deleted " + absolutePath);
                } else {
                    addError("Could not delete " + absolutePath);
                }
                filesDeleted = filesDeleted + 1;
            } else {
                addInfo("Will keep " + absolutePath);
            }
        }
        addInfo("Deleted " + filesDeleted + " files.");
    }

    public String getActiveFileName() {
        // From FixedWindowRollingPolicy.
        return getParentsRawFileProperty();
    }

    /**
     * @return
     */
    public long getDaysToKeep() {
        return daysToKeep;
    }

    /**
     * @param daysToKeep
     */
    public void setDaysToKeep(long daysToKeep) {
        this.daysToKeep = daysToKeep;
    }
}
