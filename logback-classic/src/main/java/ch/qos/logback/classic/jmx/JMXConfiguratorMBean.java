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
package ch.qos.logback.classic.jmx;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

import ch.qos.logback.core.joran.spi.JoranException;

public interface JMXConfiguratorMBean {

    void reloadDefaultConfiguration() throws JoranException;

    void reloadByFileName(String fileName) throws JoranException, FileNotFoundException;

    void reloadByURL(URL url) throws JoranException;

    void setLoggerLevel(String loggerName, String levelStr);

    String getLoggerLevel(String loggerName);

    String getLoggerEffectiveLevel(String loggerName);

    List<String> getLoggerList();

    List<String> getStatuses();
}
