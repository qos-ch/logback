/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2025, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.FileUtil;

import java.io.File;

abstract public class CompressionStrategyBase extends ContextAwareBase implements CompressionStrategy {

    static final int BUFFER_SIZE = 8192;

    void createMissingTargetDirsIfNecessary(File file) {
        boolean result = FileUtil.createMissingParentDirectories(file);
        if (!result) {
            addError("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
        }
    }
}
