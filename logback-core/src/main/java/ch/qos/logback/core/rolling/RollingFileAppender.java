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
import static ch.qos.logback.core.CoreConstants.MORE_INFO_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.util.ContextUtil;

/**
 * <code>RollingFileAppender</code> extends {@link FileAppender} to backup the
 * log files depending on {@link RollingPolicy} and {@link TriggeringPolicy}.
 * 
 * <p>
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#RollingFileAppender
 *
 * @author Heinz Richter
 * @author Ceki G&uuml;lc&uuml;
 */
public class RollingFileAppender<E> extends FileAppender<E> {
    File currentlyActiveFile;
    TriggeringPolicy<E> triggeringPolicy;
    RollingPolicy rollingPolicy;

    static private String RFA_NO_TP_URL = CODES_URL + "#rfa_no_tp";
    static private String RFA_NO_RP_URL = CODES_URL + "#rfa_no_rp";
    static private String COLLISION_URL = CODES_URL + "#rfa_collision";
    static private String RFA_LATE_FILE_URL = CODES_URL + "#rfa_file_after";

    public void start() {
        if (triggeringPolicy == null) {
            addWarn("No TriggeringPolicy was set for the RollingFileAppender named " + getName());
            addWarn(MORE_INFO_PREFIX + RFA_NO_TP_URL);
            return;
        }
        if (!triggeringPolicy.isStarted()) {
            addWarn("TriggeringPolicy has not started. RollingFileAppender will not start");
            return;
        }

        if (checkForCollisionsInPreviousRollingFileAppenders()) {
            addError("Collisions detected with FileAppender/RollingAppender instances defined earlier. Aborting.");
            addError(MORE_INFO_PREFIX + COLLISION_WITH_EARLIER_APPENDER_URL);
            return;
        }

        // we don't want to void existing log files
        if (!append) {
            addWarn("Append mode is mandatory for RollingFileAppender. Defaulting to append=true.");
            append = true;
        }

        if (rollingPolicy == null) {
            addError("No RollingPolicy was set for the RollingFileAppender named " + getName());
            addError(MORE_INFO_PREFIX + RFA_NO_RP_URL);
            return;
        }

        // sanity check for http://jira.qos.ch/browse/LOGBACK-796
        if (checkForFileAndPatternCollisions()) {
            addError("File property collides with fileNamePattern. Aborting.");
            addError(MORE_INFO_PREFIX + COLLISION_URL);
            return;
        }

        if (isPrudent()) {
            if (rawFileProperty() != null) {
                addWarn("Setting \"File\" property to null on account of prudent mode");
                setFile(null);
            }
            if (rollingPolicy.getCompressionMode() != CompressionMode.NONE) {
                addError("Compression is not supported in prudent mode. Aborting");
                return;
            }
        }

        currentlyActiveFile = new File(getFile());
        addInfo("Active log file name: " + getFile());
        super.start();
    }

    private boolean checkForFileAndPatternCollisions() {
        if (triggeringPolicy instanceof RollingPolicyBase) {
            final RollingPolicyBase base = (RollingPolicyBase) triggeringPolicy;
            final FileNamePattern fileNamePattern = base.fileNamePattern;
            // no use checking if either fileName or fileNamePattern are null
            if (fileNamePattern != null && fileName != null) {
                String regex = fileNamePattern.toRegex();
                return fileName.matches(regex);
            }
        }
        return false;
    }

    private boolean checkForCollisionsInPreviousRollingFileAppenders() {
        boolean collisionResult = false;
        if (triggeringPolicy instanceof RollingPolicyBase) {
            final RollingPolicyBase base = (RollingPolicyBase) triggeringPolicy;
            final FileNamePattern fileNamePattern = base.fileNamePattern;
            boolean collisionsDetected = innerCheckForFileNamePatternCollisionInPreviousRFA(fileNamePattern);
            if (collisionsDetected)
                collisionResult = true;
        }
        return collisionResult;
    }

    private boolean innerCheckForFileNamePatternCollisionInPreviousRFA(FileNamePattern fileNamePattern) {
        boolean collisionsDetected = false;
        @SuppressWarnings("unchecked")
        Map<String, FileNamePattern> map = (Map<String, FileNamePattern>) context.getObject(CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP);
        if (map == null) {
            return collisionsDetected;
        }
        for (Entry<String, FileNamePattern> entry : map.entrySet()) {
            if (fileNamePattern.equals(entry.getValue())) {
                addErrorForCollision("FileNamePattern", entry.getValue().toString(), entry.getKey());
                collisionsDetected = true;
            }
        }
        if (name != null) {
            map.put(getName(), fileNamePattern);
        }
        return collisionsDetected;
    }

    @Override
    public void stop() {
        super.stop();
        
        if (rollingPolicy != null)
            rollingPolicy.stop();
        if (triggeringPolicy != null)
            triggeringPolicy.stop();

        Map<String, FileNamePattern> map = ContextUtil.getFilenamePatternCollisionMap(context);
        if (map != null && getName() != null)
            map.remove(getName());

    }

    @Override
    public void setFile(String file) {
        // http://jira.qos.ch/browse/LBCORE-94
        // allow setting the file name to null if mandated by prudent mode
        if (file != null && ((triggeringPolicy != null) || (rollingPolicy != null))) {
            addError("File property must be set before any triggeringPolicy or rollingPolicy properties");
            addError(MORE_INFO_PREFIX + RFA_LATE_FILE_URL);
        }
        super.setFile(file);
    }

    @Override
    public String getFile() {
        return rollingPolicy.getActiveFileName();
    }

    /**
     * Implemented by delegating most of the rollover work to a rolling policy.
     */
    public void rollover() {
        lock.lock();
        try {
            // Note: This method needs to be synchronized because it needs exclusive
            // access while it closes and then re-opens the target file.
            //
            // make sure to close the hereto active log file! Renaming under windows
            // does not work for open files.
            this.closeOutputStream();
            attemptRollover();
            attemptOpenFile();
        } finally {
            lock.unlock();
        }
    }

    private void attemptOpenFile() {
        try {
            // update the currentlyActiveFile LOGBACK-64
            currentlyActiveFile = new File(rollingPolicy.getActiveFileName());

            // This will also close the file. This is OK since multiple close operations are safe.
            this.openFile(rollingPolicy.getActiveFileName());
        } catch (IOException e) {
            addError("setFile(" + fileName + ", false) call failed.", e);
        }
    }

    private void attemptRollover() {
        try {
            rollingPolicy.rollover();
        } catch (RolloverFailure rf) {
            addWarn("RolloverFailure occurred. Deferring roll-over.");
            // we failed to roll-over, let us not truncate and risk data loss
            this.append = true;
        }
    }

    /**
    * This method differentiates RollingFileAppender from its super class.
    */
    @Override
    protected void subAppend(E event) {
        // The roll-over check must precede actual writing. This is the
        // only correct behavior for time driven triggers.

        // We need to synchronize on triggeringPolicy so that only one rollover
        // occurs at a time
        synchronized (triggeringPolicy) {
            if (triggeringPolicy.isTriggeringEvent(currentlyActiveFile, event)) {
                rollover();
            }
        }

        super.subAppend(event);
    }

    public RollingPolicy getRollingPolicy() {
        return rollingPolicy;
    }

    public TriggeringPolicy<E> getTriggeringPolicy() {
        return triggeringPolicy;
    }

    /**
     * Sets the rolling policy. In case the 'policy' argument also implements
     * {@link TriggeringPolicy}, then the triggering policy for this appender is
     * automatically set to be the policy argument.
     *
     * @param policy
     */
    @SuppressWarnings("unchecked")
    public void setRollingPolicy(RollingPolicy policy) {
        rollingPolicy = policy;
        if (rollingPolicy instanceof TriggeringPolicy) {
            triggeringPolicy = (TriggeringPolicy<E>) policy;
        }

    }

    public void setTriggeringPolicy(TriggeringPolicy<E> policy) {
        triggeringPolicy = policy;
        if (policy instanceof RollingPolicy) {
            rollingPolicy = (RollingPolicy) policy;
        }
    }
}
