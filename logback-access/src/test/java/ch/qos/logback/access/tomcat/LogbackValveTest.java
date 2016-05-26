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
        System.setProperty(LogbackValve.CATALINA_BASE_KEY, AccessTestConstants.JORAN_INPUT_PREFIX + "tomcat/");
        final String fileName = "logback-access.xml";
        setupValve(fileName);
        valve.start();
        checker.assertContainsMatch("Found configuration file");
        checker.assertContainsMatch("Done configuring");
        checker.assertIsErrorFree();
    }

    @Test
    public void fileUnderCatalinaHomeShouldBeFound() throws LifecycleException {
        System.setProperty(LogbackValve.CATALINA_HOME_KEY, AccessTestConstants.JORAN_INPUT_PREFIX + "tomcat/");
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
        checker.assertContainsMatch("Found ." + fileName + ". as a resource.");
        checker.assertContainsMatch("Done configuring");
        checker.assertIsErrorFree();
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