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
package ch.qos.logback.core.joran.spi;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConfigurationWatchListTest {

    @Test
    // See http://jira.qos.ch/browse/LBCORE-119
    public void fileToURLAndBack() throws MalformedURLException {
        final File file = new File("a b.xml");
        final URL url = file.toURI().toURL();
        final ConfigurationWatchList cwl = new ConfigurationWatchList();
        final File back = cwl.convertToFile(url);
        assertEquals(file.getName(), back.getName());
    }
}
