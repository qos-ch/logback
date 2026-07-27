/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
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
     * @deprecated replaced by #fileNamePatternStrToCompressionMode(String) followed by #outputCompressionModeMessage(CompressionMode)
     */
    @Deprecated
    protected void determineCompressionMode() {
        compressionMode = fileNamePatternStrToCompressionMode(fileNamePatternStr);
        outputCompressionModeMessage(compressionMode);
    }

    protected void outputCompressionModeMessage(CompressionMode compressionMode) {

        switch (compressionMode) {
            case GZ:
                addInfo("Will use gz compression");
                break;
            case ZIP:
                addInfo("Will use zip compression");
                break;
            case XZ:
                addInfo("Will use xz compression");
                break;
            case NONE:
                addInfo("No compression will be used");
                break;
        }
    }

    /**
     * Given the FileNamePattern string, this method determines the compression mode
     * depending on last letters of the fileNamePatternStr. Patterns ending with .gz
     * imply GZIP compression, endings with '.zip' imply ZIP compression, endings with
     * .xz imply XZ compression. Otherwise and by default, there is no compression.
     *
     * @since 1.6.1
     */
    protected CompressionMode fileNamePatternStrToCompressionMode(String aFileNamePatternStr) {
        if (aFileNamePatternStr.endsWith(CompressionMode.GZ_SUFFIX)) {
            return CompressionMode.GZ;
        } else if (aFileNamePatternStr.endsWith(CompressionMode.ZIP_SUFFIX)) {
            return CompressionMode.ZIP;
        } else if (aFileNamePatternStr.endsWith(CompressionMode.XZ_SUFFIX)) {
            return CompressionMode.XZ;
        } else {
            return CompressionMode.NONE;
        }
    }


    /**
     * If compression mode is XZ but the XZ library is missing, then fallback to GZ compression.
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
