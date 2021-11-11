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
package ch.qos.logback.core.joran;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.TopElementAction;
import ch.qos.logback.core.joran.action.ext.IncAction;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.CoreTestConstants;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.TrivialStatusListener;

public class TrivialConfiguratorTest {

    Context context = new ContextBase();
    HashMap<ElementSelector, Action> rulesMap = new HashMap<>();

    @Before
    public void setUp() {
        // rule store is case insensitve
        rulesMap.put(new ElementSelector("x"), new TopElementAction());
        rulesMap.put(new ElementSelector("x/inc"), new IncAction());

    }

    public void doTest(final String filename) throws Exception {
        final TrivialConfigurator trivialConfigurator = new TrivialConfigurator(rulesMap);

        trivialConfigurator.setContext(context);
        trivialConfigurator.doConfigure(filename);
    }

    @Test
    public void smoke() throws Exception {
        final int oldBeginCount = IncAction.beginCount;
        final int oldEndCount = IncAction.endCount;
        final int oldErrorCount = IncAction.errorCount;
        doTest(CoreTestConstants.TEST_SRC_PREFIX + "input/joran/" + "inc.xml");
        assertEquals(oldErrorCount, IncAction.errorCount);
        assertEquals(oldBeginCount + 1, IncAction.beginCount);
        assertEquals(oldEndCount + 1, IncAction.endCount);
    }

    @Test
    public void inexistentFile() {
        final TrivialStatusListener tsl = new TrivialStatusListener();
        tsl.start();
        final String filename = CoreTestConstants.TEST_SRC_PREFIX + "input/joran/" + "nothereBLAH.xml";
        context.getStatusManager().add(tsl);
        try {
            doTest(filename);
        } catch (final Exception e) {
            assertTrue(e.getMessage().startsWith("Could not open ["));
        }
        assertTrue(tsl.list.size() + " should be greater than or equal to 1", tsl.list.size() >= 1);
        final Status s0 = tsl.list.get(0);
        assertTrue(s0.getMessage().startsWith("Could not open ["));
    }

    @Test
    public void illFormedXML() {
        final TrivialStatusListener tsl = new TrivialStatusListener();
        tsl.start();
        final String filename = CoreTestConstants.TEST_SRC_PREFIX + "input/joran/" + "illformed.xml";
        context.getStatusManager().add(tsl);
        try {
            doTest(filename);
        } catch (final Exception e) {
        }
        assertEquals(2, tsl.list.size());
        final Status s0 = tsl.list.get(0);
        assertTrue(s0.getMessage().startsWith(CoreConstants.XML_PARSING));
    }

    @Test
    public void lbcore105() throws IOException, JoranException {
        final String jarEntry = "buzz.xml";
        final File jarFile = makeRandomJarFile();
        fillInJarFile(jarFile, jarEntry);
        final URL url = asURL(jarFile, jarEntry);
        final TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
        tc.doConfigure(url);
        // deleting an open file fails
        assertTrue(jarFile.delete());
        assertFalse(jarFile.exists());
    }

    @Test
    public void lbcore127() throws IOException, JoranException {
        final String jarEntry = "buzz.xml";
        final String jarEntry2 = "lightyear.xml";

        final File jarFile = makeRandomJarFile();
        fillInJarFile(jarFile, jarEntry, jarEntry2);

        final URL url1 = asURL(jarFile, jarEntry);
        final URL url2 = asURL(jarFile, jarEntry2);

        final URLConnection urlConnection2 = url2.openConnection();
        urlConnection2.setUseCaches(false);
        final InputStream is = urlConnection2.getInputStream();

        final TrivialConfigurator tc = new TrivialConfigurator(rulesMap);
        tc.setContext(context);
        tc.doConfigure(url1);

        is.read();
        is.close();

        // deleting an open file fails
        assertTrue(jarFile.delete());
        assertFalse(jarFile.exists());
    }

    File makeRandomJarFile() {
        final File outputDir = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
        outputDir.mkdirs();
        final int randomPart = RandomUtil.getPositiveInt();
        return new File(CoreTestConstants.OUTPUT_DIR_PREFIX + "foo-" + randomPart + ".jar");
    }

    private void fillInJarFile(final File jarFile, final String jarEntryName) throws IOException {
        fillInJarFile(jarFile, jarEntryName, null);
    }

    private void fillInJarFile(final File jarFile, final String jarEntryName1, final String jarEntryName2) throws IOException {
        final JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile));
        jos.putNextEntry(new ZipEntry(jarEntryName1));
        jos.write("<x/>".getBytes());
        jos.closeEntry();
        if (jarEntryName2 != null) {
            jos.putNextEntry(new ZipEntry(jarEntryName2));
            jos.write("<y/>".getBytes());
            jos.closeEntry();
        }
        jos.close();
    }

    URL asURL(final File jarFile, final String jarEntryName) throws IOException {
        final URL innerURL = jarFile.toURI().toURL();
        return new URL("jar:" + innerURL + "!/" + jarEntryName);
    }

}
