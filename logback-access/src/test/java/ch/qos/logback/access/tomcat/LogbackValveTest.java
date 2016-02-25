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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.ContainerBase;
import org.junit.After;
import org.junit.Test;

import ch.qos.logback.access.AccessTestConstants;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;

public class LogbackValveTest {

    LogbackValve valve = new LogbackValve();
    StatusChecker checker = new StatusChecker(valve);

    
    @After 
    public void tearDown() {
        System.clearProperty(LogbackValve.CATALINA_BASE_KEY); 
        System.clearProperty(LogbackValve.CATALINA_HOME_KEY); 
    }
    
    @Test
    public void nonExistingConfigFileShouldResultInWarning() throws LifecycleException {
        final String resourceName = "logback-test2-config.xml";
        setupValve(resourceName);
        valve.start();
        checker.assertContainsMatch(Status.WARN, "Failed to find valid");
    }

    @Test
    public void fileUnderCatalinaBaseShouldBeFound() throws LifecycleException {
        System.setProperty(LogbackValve.CATALINA_BASE_KEY, AccessTestConstants.JORAN_INPUT_PREFIX+"tomcat/");
        final String fileName = "logback-access.xml";
        setupValve(fileName);
        valve.start();
        checker.assertContainsMatch("Found configuration file");
        checker.assertContainsMatch("Done configuring");
        checker.assertIsErrorFree();
    }
    
    @Test
    public void fileUnderCatalinaHomeShouldBeFound() throws LifecycleException {
        System.setProperty(LogbackValve.CATALINA_HOME_KEY, AccessTestConstants.JORAN_INPUT_PREFIX+"tomcat/");
        final String fileName = "logback-access.xml";
        setupValve(fileName);
        valve.start();
        checker.assertContainsMatch("Found configuration file");
        checker.assertContainsMatch("Done configuring");
        checker.assertIsErrorFree();
    }    

    
    @Test
    public void resourceShouldBeFound() throws LifecycleException {
        final String fileName = "logback-asResource.xml";
        setupValve(fileName);
        valve.start();
        checker.assertContainsMatch("Found ."+fileName+". as a resource.");
        checker.assertContainsMatch("Done configuring");
        checker.assertIsErrorFree();
        
        //avoid double printing of status messages
    }    

    
    // // assertFalse("configuration failed for resource '" + errorDetected + "'", resultStatus.get());
    // }
    //
    // @Test
    // public void testNonExistingClasspathResourceWithExistingFile() throws LifecycleException, IOException {
    // File tmpFile = File.createTempFile(getClass().getName(), "valve");
    // copyStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("logback-test-config.xml"),
    // tmpFile);
    // final String resourceName = tmpFile.getAbsolutePath();
    // setupValve(resourceName);
    // valve.start();
    //
    // // assertFalse("configuration succeeded for resource '" + resourceName + "'", errorDetected.get());
    // }

    private void copyStreamToFile(InputStream stream, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
    }

    private void setupValve(final String resourceName) {
        valve.setFilename(resourceName);
        valve.setName("test");
        valve.setContainer(new ContainerBase() {
            @Override
            protected String getObjectNameKeyProperties() {
                return "getObjectNameKeyProperties-test";
            }
        });
    }
}