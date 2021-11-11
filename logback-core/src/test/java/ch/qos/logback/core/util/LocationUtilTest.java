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
package ch.qos.logback.core.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

/**
 * Unit tests for {@link LocationUtil}.
 *
 * @author Carl Harris
 */
public class LocationUtilTest {

    private static final String TEST_CLASSPATH_RESOURCE = "util/testResource.txt";
    private static final String TEST_PATTERN = "TEST RESOURCE";

    @Test
    public void testImplicitClasspathUrl() throws Exception {
        final URL url = LocationUtil.urlForResource(TEST_CLASSPATH_RESOURCE);
        validateResource(url);
    }

    @Test
    public void testExplicitClasspathUrl() throws Exception {
        final URL url = LocationUtil.urlForResource(LocationUtil.CLASSPATH_SCHEME + TEST_CLASSPATH_RESOURCE);
        validateResource(url);
    }

    @Test
    public void testExplicitClasspathUrlWithLeadingSlash() throws Exception {
        final URL url = LocationUtil.urlForResource(LocationUtil.CLASSPATH_SCHEME + "/" + TEST_CLASSPATH_RESOURCE);
        validateResource(url);
    }

    @Test(expected = MalformedURLException.class)
    public void testExplicitClasspathUrlEmptyPath() throws Exception {
        LocationUtil.urlForResource(LocationUtil.CLASSPATH_SCHEME);
    }

    @Test(expected = MalformedURLException.class)
    public void testExplicitClasspathUrlWithRootPath() throws Exception {
        LocationUtil.urlForResource(LocationUtil.CLASSPATH_SCHEME + "/");
    }

    @Test
    public void testFileUrl() throws Exception {
        final File file = File.createTempFile("testResource", ".txt");
        file.deleteOnExit();
        final PrintWriter writer = new PrintWriter(file);
        writer.println(TEST_PATTERN);
        writer.close();
        final URL url = file.toURI().toURL();
        validateResource(url);
    }

    private void validateResource(final URL url) throws IOException {
        final InputStream inputStream = url.openStream();
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            final String line = reader.readLine();
            assertEquals(TEST_PATTERN, line);
        } finally {
            try {
                inputStream.close();
            } catch (final IOException ex) {
                // ignore close exception
                ex.printStackTrace(System.err);
            }
        }
    }

}
