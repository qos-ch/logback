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

import java.io.File;
import java.util.Date;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.helper.ArchiveRemover;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileFilterUtil;
import ch.qos.logback.core.rolling.helper.SizeAndTimeBasedArchiveRemover;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.DefaultInvocationGate;
import ch.qos.logback.core.util.InvocationGate;

@NoAutoStart
public class SizeAndTimeBasedFNATP<E> extends TimeBasedFileNamingAndTriggeringPolicyBase<E> {

    int currentPeriodsCounter = 0;
    FileSize maxFileSize;
    // String maxFileSizeAsString;

    long nextSizeCheck = 0;
    static String MISSING_INT_TOKEN = "Missing integer token, that is %i, in FileNamePattern [";
    static String MISSING_DATE_TOKEN = "Missing date token, that is %d, in FileNamePattern [";

    @Override
    public void start() {
        // we depend on certain fields having been initialized in super class
        super.start();

        addWarn("SizeAndTimeBasedFNATP is deprecated. Use SizeAndTimeBasedRollingPolicy instead");
        
        if (!super.isErrorFree())
            return;

        
        if (maxFileSize == null) {
            addError("maxFileSize property is mandatory.");
            withErrors();
        }

        if (!validateDateAndIntegerTokens()) {
            withErrors();
            return;
        }

        archiveRemover = createArchiveRemover();
        archiveRemover.setContext(context);

        // we need to get the correct value of currentPeriodsCounter.
        // usually the value is 0, unless the appender or the application
        // is stopped and restarted within the same period
        String regex = tbrp.fileNamePattern.toRegexForFixedDate(dateInCurrentPeriod);
        String stemRegex = FileFilterUtil.afterLastSlash(regex);

        computeCurrentPeriodsHighestCounterValue(stemRegex);

        if (isErrorFree()) {
            started = true;
        }
    }

    private boolean validateDateAndIntegerTokens() {
        boolean inError = false;
        if (tbrp.fileNamePattern.getIntegerTokenConverter() == null) {
            inError = true;
            addError(MISSING_INT_TOKEN + tbrp.fileNamePatternStr + "]");
            addError(CoreConstants.SEE_MISSING_INTEGER_TOKEN);
        }
        if (tbrp.fileNamePattern.getPrimaryDateTokenConverter() == null) {
            inError = true;
            addError(MISSING_DATE_TOKEN + tbrp.fileNamePatternStr + "]");
        }

        return !inError;
    }

    protected ArchiveRemover createArchiveRemover() {
        return new SizeAndTimeBasedArchiveRemover(tbrp.fileNamePattern, rc);
    }

    void computeCurrentPeriodsHighestCounterValue(final String stemRegex) {
        File file = new File(getCurrentPeriodsFileNameWithoutCompressionSuffix());
        File parentDir = file.getParentFile();

        File[] matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(parentDir, stemRegex);

        if (matchingFileArray == null || matchingFileArray.length == 0) {
            currentPeriodsCounter = 0;
            return;
        }
        currentPeriodsCounter = FileFilterUtil.findHighestCounter(matchingFileArray, stemRegex);

        // if parent raw file property is not null, then the next
        // counter is max found counter+1
        if (tbrp.getParentsRawFileProperty() != null || (tbrp.compressionMode != CompressionMode.NONE)) {
            // TODO test me
            currentPeriodsCounter++;
        }
    }

    InvocationGate invocationGate = new DefaultInvocationGate();

    @Override
    public boolean isTriggeringEvent(File activeFile, final E event) {

        long time = getCurrentTime();

        // first check for roll-over based on time
        if (time >= nextCheck) {
            Date dateInElapsedPeriod = dateInCurrentPeriod;
            elapsedPeriodsFileName = tbrp.fileNamePatternWCS.convertMultipleArguments(dateInElapsedPeriod, currentPeriodsCounter);
            currentPeriodsCounter = 0;
            setDateInCurrentPeriod(time);
            computeNextCheck();
            return true;
        }

        // next check for roll-over based on size
        if (invocationGate.isTooSoon(time)) {
            return false;
        }

        if (activeFile == null) {
            addWarn("activeFile == null");
            return false;
        }
        if (maxFileSize == null) {
            addWarn("maxFileSize = null");
            return false;
        }
        if (activeFile.length() >= maxFileSize.getSize()) {

            elapsedPeriodsFileName = tbrp.fileNamePatternWCS.convertMultipleArguments(dateInCurrentPeriod, currentPeriodsCounter);
            currentPeriodsCounter++;
            return true;
        }

        return false;
    }

    @Override
    public String getCurrentPeriodsFileNameWithoutCompressionSuffix() {
        return tbrp.fileNamePatternWCS.convertMultipleArguments(dateInCurrentPeriod, currentPeriodsCounter);
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

}
