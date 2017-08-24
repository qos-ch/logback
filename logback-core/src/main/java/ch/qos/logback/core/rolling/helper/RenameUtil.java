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
package ch.qos.logback.core.rolling.helper;

import java.io.File;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.FileUtil;

/**
 * Utility class to help solving problems encountered while renaming files.
 *
 * @author Ceki Gulcu
 */
public class RenameUtil extends ContextAwareBase {

    static String RENAMING_ERROR_URL = CoreConstants.CODES_URL + "#renamingError";

    /**
     * A relatively robust file renaming method which in case of failure due to
     * src and target being on different volumes, falls back onto
     * renaming by copying.
     *
     * @param src
     * @param target
     * @throws RolloverFailure
     */
    public void rename(String src, String target) throws RolloverFailure {
        if (src.equals(target)) {
            addWarn("Source and target files are the same [" + src + "]. Skipping.");
            return;
        }
        File srcFile = new File(src);

        if (srcFile.exists()) {
            File targetFile = new File(target);
            createMissingTargetDirsIfNecessary(targetFile);

            addInfo("Renaming file [" + srcFile + "] to [" + targetFile + "]");

            boolean result = srcFile.renameTo(targetFile);

            if (!result) {
                addWarn("Failed to rename file [" + srcFile + "] as [" + targetFile + "].");
                Boolean areOnDifferentVolumes = areOnDifferentVolumes(srcFile, targetFile);
                if (Boolean.TRUE.equals(areOnDifferentVolumes)) {
                    addWarn("Detected different file systems for source [" + src + "] and target [" + target + "]. Attempting rename by copying.");
                    renameByCopying(src, target);
                    return;
                } else {
                    addWarn("Please consider leaving the [file] option of " + RollingFileAppender.class.getSimpleName() + " empty.");
                    addWarn("See also " + RENAMING_ERROR_URL);
                }
            }
        } else {
            throw new RolloverFailure("File [" + src + "] does not exist.");
        }
    }

    
    
    /**
     * Attempts to determine whether both files are on different volumes. Returns true if we could determine that
     * the files are on different volumes. Returns false otherwise or if an error occurred while doing the check.
     *
     * @param srcFile
     * @param targetFile
     * @return true if on different volumes, false otherwise or if an error occurred
     */
    Boolean areOnDifferentVolumes(File srcFile, File targetFile) throws RolloverFailure {
        if (!EnvUtil.isJDK7OrHigher())
            return false;

        // target file is not certain to exist but its parent has to exist given the call hierarchy of this method
        File parentOfTarget = targetFile.getAbsoluteFile().getParentFile();
        
        if(parentOfTarget == null) {
            addWarn("Parent of target file ["+targetFile+"] is null");
            return null;
        }
        if(!parentOfTarget.exists()) {
            addWarn("Parent of target file ["+targetFile+"] does not exist");
            return null;
        }
        
        try {
            boolean onSameFileStore = FileStoreUtil.areOnSameFileStore(srcFile, parentOfTarget);
            return !onSameFileStore;
        } catch (RolloverFailure rf) {
            addWarn("Error while checking file store equality", rf);
            return null;
        }
    }

    public void renameByCopying(String src, String target) throws RolloverFailure {

        FileUtil fileUtil = new FileUtil(getContext());
        fileUtil.copy(src, target);

        File srcFile = new File(src);
        if (!srcFile.delete()) {
            addWarn("Could not delete " + src);
        }

    }

    void createMissingTargetDirsIfNecessary(File toFile) throws RolloverFailure {
        boolean result = FileUtil.createMissingParentDirectories(toFile);
        if (!result) {
            throw new RolloverFailure("Failed to create parent directories for [" + toFile.getAbsolutePath() + "]");
        }
    }

    @Override
    public String toString() {
        return "c.q.l.co.rolling.helper.RenameUtil";
    }
}
