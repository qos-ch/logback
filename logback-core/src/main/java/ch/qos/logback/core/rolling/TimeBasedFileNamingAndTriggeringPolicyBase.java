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
package ch.qos.logback.core.rolling;

import static ch.qos.logback.core.CoreConstants.CODES_URL;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.DateTokenConverter;
import ch.qos.logback.core.rolling.helper.RollingCalendar;
import ch.qos.logback.core.spi.ContextAwareBase;

abstract public class TimeBasedFileNamingAndTriggeringPolicyBase<E> extends ContextAwareBase implements TimeBasedFileNamingAndTriggeringPolicy<E> {

    static private String COLLIDING_DATE_FORMAT_URL = CODES_URL + "#rfa_collision_in_dateFormat";

    protected TimeBasedRollingPolicy<E> tbrp;

    protected ArchiveRemover archiveRemover = null;
    protected String elapsedPeriodsFileName;
    protected RollingCalendar rc;

    protected long artificialCurrentTime = -1;
    protected Date dateInCurrentPeriod = null;

    protected long nextCheck;
    protected boolean started = false;
    protected boolean errorFree = true;

    public boolean isStarted() {
        return started;
    }

    public void start() {
        DateTokenConverter<Object> dtc = tbrp.fileNamePattern.getPrimaryDateTokenConverter();
        if (dtc == null) {
            throw new IllegalStateException("FileNamePattern [" + tbrp.fileNamePattern.getPattern() + "] does not contain a valid DateToken");
        }

        if (dtc.getTimeZone() != null) {
            rc = new RollingCalendar(dtc.getDatePattern(), dtc.getTimeZone(), Locale.getDefault());
        } else {
            rc = new RollingCalendar(dtc.getDatePattern());
        }
        addInfo("The date pattern is '" + dtc.getDatePattern() + "' from file name pattern '" + tbrp.fileNamePattern.getPattern() + "'.");
        rc.printPeriodicity(this);

        if (!rc.isCollisionFree()) {
            addError("The date format in FileNamePattern will result in collisions in the names of archived log files.");
            addError(CoreConstants.MORE_INFO_PREFIX + COLLIDING_DATE_FORMAT_URL);
            withErrors();
            return;
        }

        setDateInCurrentPeriod(new Date(getCurrentTime()));
        if (tbrp.getParentsRawFileProperty() != null) {
            File currentFile = new File(tbrp.getParentsRawFileProperty());
            if (currentFile.exists() && currentFile.canRead()) {
                setDateInCurrentPeriod(new Date(currentFile.lastModified()));
            }
        }
        addInfo("Setting initial period to " + dateInCurrentPeriod);
        computeNextCheck();
    }

    public void stop() {
        started = false;
    }

    protected void computeNextCheck() {
        nextCheck = rc.getNextTriggeringDate(dateInCurrentPeriod).getTime();
    }

    protected void setDateInCurrentPeriod(long now) {
        dateInCurrentPeriod.setTime(now);
    }

    // allow Test classes to act on the dateInCurrentPeriod field to simulate old
    // log files needing rollover
    public void setDateInCurrentPeriod(Date _dateInCurrentPeriod) {
        this.dateInCurrentPeriod = _dateInCurrentPeriod;
    }

    public String getElapsedPeriodsFileName() {
        return elapsedPeriodsFileName;
    }

    public String getCurrentPeriodsFileNameWithoutCompressionSuffix() {
        return tbrp.fileNamePatternWCS.convert(dateInCurrentPeriod);
    }

    public void setCurrentTime(long timeInMillis) {
        artificialCurrentTime = timeInMillis;
    }

    public long getCurrentTime() {
        // if time is forced return the time set by user
        if (artificialCurrentTime >= 0) {
            return artificialCurrentTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    public void setTimeBasedRollingPolicy(TimeBasedRollingPolicy<E> _tbrp) {
        this.tbrp = _tbrp;

    }

    public ArchiveRemover getArchiveRemover() {
        return archiveRemover;
    }

    protected void withErrors() {
        errorFree = false;
    }
    
    protected boolean isErrorFree() {
        return errorFree;
    }

}
