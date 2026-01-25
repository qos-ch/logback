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

package ch.qos.logback.core.rolling.helper;

import ch.qos.logback.core.spi.ContextAware;

/**
 * This interface was introduced in order to support for pluggable
 * compression methods.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.5.18
 */
public interface CompressionStrategy extends ContextAware {

    void compress(String originalFileName, String compressedFileName, String innerEntryName);
}
