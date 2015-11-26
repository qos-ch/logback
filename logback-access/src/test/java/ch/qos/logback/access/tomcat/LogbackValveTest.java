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
package ch.qos.logback.access.tomcat;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.ContainerBase;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class LogbackValveTest {

    @Test
    public void testNonExistingClasspathResource() throws LifecycleException {
        final String resourceName = "logback-test2-config.xml";
        final AtomicBoolean errorDetected = new AtomicBoolean(false);
        LogbackValve valve = setupValve(resourceName, errorDetected);
        valve.start();

        assertTrue("configuration succeeded for resource '" + resourceName +"'", errorDetected.get());
    }

    @Test
    public void testExistingClasspathResource() throws LifecycleException {
        final String errorDetected = "logback-test-config.xml";
        final AtomicBoolean resultStatus = new AtomicBoolean(false);
        LogbackValve valve = setupValve(errorDetected, resultStatus);
        valve.start();

        assertFalse("configuration failed for resource '" + errorDetected +"'",resultStatus.get());
    }

    @Test
    public void testNonExistingClasspathResourceWithExistingFile() throws LifecycleException, IOException {
        File tmpFile = File.createTempFile(getClass().getName(),"valve");
        copyStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("logback-test-config.xml"), tmpFile);
        final String resourceName = tmpFile.getAbsolutePath();
        final AtomicBoolean errorDetected = new AtomicBoolean(false);
        LogbackValve valve = setupValve(resourceName, errorDetected);
        valve.start();

        assertFalse("configuration succeeded for resource '" + resourceName +"'", errorDetected.get());
    }

    private void copyStreamToFile(InputStream stream, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
    }

    private LogbackValve setupValve(final String resourceName, final AtomicBoolean errorDetected) {
        LogbackValve valve = new LogbackValve();
        valve.setFilename(resourceName);
        valve.getStatusManager().add(new StatusListener() {
            @Override
            public void addStatusEvent(Status status) {
                boolean currentStatus = status.getMessage().contains(resourceName);
                if (currentStatus) {
                    errorDetected.set(true);
                }
            }
        });
        valve.setContainer(new ContainerBase() {
            @Override
            protected String getObjectNameKeyProperties() {
                return "getObjectNameKeyProperties-test";
            }
        });

        return valve;
    }
}