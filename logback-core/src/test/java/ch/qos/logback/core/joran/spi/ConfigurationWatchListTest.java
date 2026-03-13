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
package ch.qos.logback.core.joran.spi;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
class ConfigurationWatchListTest {

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("relativeFilePaths")
    void fileToURLAndBack(String relativePath) throws MalformedURLException {
        File file = new File(relativePath);
        URL url = file.toURI().toURL();
        ConfigurationWatchList cwl = new ConfigurationWatchList();
        File back = cwl.convertToFile(url);
        assertEquals(file.getName(), back.getName());
    }

    static Stream<Arguments> relativeFilePaths() {
        return Stream.of(
            arguments(named("path with space", "a b.xml")),
            arguments(named("path with plus sign", "a+b.xml"))
        );
    }

}
