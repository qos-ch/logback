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

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.helper.CompressionMode;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.ContextAwareBase;

import static ch.qos.logback.core.util.Loader.isClassLoadable;

/**
 * Implements methods common to most, it not all, rolling policies. Currently
 * such methods are limited to a compression mode getter/setter.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public abstract class RollingPolicyBase extends ContextAwareBase implements RollingPolicy {
    protected CompressionMode compressionMode = CompressionMode.NONE;

    FileNamePattern fileNamePattern;
    // fileNamePatternStr is always slashified, see setter
    protected String fileNamePatternStr;

    private FileAppender<?> parent;

    // use to name files within zip file, i.e. the zipEntry
    FileNamePattern zipEntryFileNamePattern;
    private boolean started;

    /**
     * Given the FileNamePattern string, this method determines the compression mode
     * depending on last letters of the fileNamePatternStr. Patterns ending with .gz
     * imply GZIP compression, endings with '.zip' imply ZIP compression, endings with
     * .xz imply XZ compression. Otherwise and by default, there is no compression.
     *
     */
    protected void determineCompressionMode() {
        if (fileNamePatternStr.endsWith(CompressionMode.GZ_SUFFIX)) {
            addInfo("Will use gz compression");
            compressionMode = CompressionMode.GZ;
        } else if (fileNamePatternStr.endsWith(CompressionMode.ZIP_SUFFIX)) {
            addInfo("Will use zip compression");
            compressionMode = CompressionMode.ZIP;
        } else if (fileNamePatternStr.endsWith(CompressionMode.XZ_SUFFIX)) {
            addInfo("Will use xz compression");
            compressionMode = CompressionMode.XZ;
        } else {
            addInfo("No compression will be used");
            compressionMode = CompressionMode.NONE;
        }
    }

    /**
     * If compression mode is XZ but the XZ librarey is missing, then fallback to GZ compresison.
     */
    protected void adjustCompressionModeAndFileNamePatternStrIfNecessary() {
        if (compressionMode == compressionMode.XZ) {
            boolean xzLibraryLoadable = isClassLoadable("org.tukaani.xz.XZOutputStream", getContext());
            if (!xzLibraryLoadable) {
                addWarn("XZ library missing, falling back to GZ compression");
                compressionMode = CompressionMode.GZ;
                fileNamePatternStr = replaceSuffix(fileNamePatternStr, CompressionMode.XZ_SUFFIX, CompressionMode.GZ_SUFFIX);
            }
        }
    }

    private String replaceSuffix(String input, String existingSuffix, String newSuffix) {
        int existingSuffixLen = existingSuffix.length();
        if (input.endsWith(existingSuffix)) {
            return input.substring(0, input.length() - existingSuffixLen) + newSuffix;
        } else {
            // unreachable code
            throw new IllegalArgumentException("[" + input + "] should end with "+existingSuffix);
        }
    }

    public void setFileNamePattern(String fnp) {
        fileNamePatternStr = fnp;
    }

    public String getFileNamePattern() {
        return fileNamePatternStr;
    }

    public CompressionMode getCompressionMode() {
        return compressionMode;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
    }

    public void setParent(FileAppender<?> appender) {
        this.parent = appender;
    }

    public boolean isParentPrudent() {
        return parent.isPrudent();
    }

    public String getParentsRawFileProperty() {
        return parent.rawFileProperty();
    }
}
