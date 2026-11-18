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
package ch.qos.logback.core.joran.util;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.ConfigurationWatchList;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for ConfigurationWatchListUtil to ensure proper handling of 
 * classpath resources and watch list initialization (Issue #1001).
 */
public class ConfigurationWatchListUtilTest {

    private Context context;
    private StatusChecker statusChecker;

    @BeforeEach
    public void setUp() {
        context = new ContextBase();
        statusChecker = new StatusChecker(context);
    }

    /**
     * Test that adding a file URL when ConfigurationWatchList is null and createCWL is false
     * does NOT log spurious messages (Issue #1001 - classpath resource inclusion without scan).
     * 
     * This simulates the case where a resource is included without scan="true",
     * which should not produce spurious warnings.
     */
    @Test
    public void testAddToWatchListWithNullCWLAndCreateCWLFalse_NoMessage() throws MalformedURLException {
        URL url = new URL("file:///tmp/logback.xml");
        
        // Initially, no ConfigurationWatchList should exist
        assertNull(ConfigurationWatchListUtil.getConfigurationWatchList(context));
        
        // Call addToWatchList with createCWL=false (happens when scan is not enabled)
        ConfigurationWatchListUtil.addToWatchList(context, url, false);
        
        // Still no ConfigurationWatchList should exist
        assertNull(ConfigurationWatchListUtil.getConfigurationWatchList(context));
        
        // No spurious watch list messages should be logged
        assertFalse(statusChecker.containsMatch("ConfigurationWatchList not initialized"),
                "Should not log watch list message when createCWL is false");
    }

    /**
     * Test that adding a file URL when ConfigurationWatchList is null and createCWL is true
     * with a watchable protocol DOES create a ConfigurationWatchList.
     */
    @Test
    public void testAddToWatchListWithNullCWLAndCreateCWLTrue_Watchable() throws MalformedURLException {
        URL url = new URL("file:///tmp/logback.xml");
        
        // Initially, no ConfigurationWatchList should exist
        assertNull(ConfigurationWatchListUtil.getConfigurationWatchList(context));
        
        // Call addToWatchList with createCWL=true and watchable protocol
        ConfigurationWatchListUtil.addToWatchList(context, url, true);
        
        // Now ConfigurationWatchList should exist
        ConfigurationWatchList cwl = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        assertNotNull(cwl, "ConfigurationWatchList should be created for watchable protocol");
        
        // URL should be added to watch list
        assertTrue(cwl.hasAtLeastOneWatchableFile(), 
                "File should be added to watch list");
    }

    /**
     * Test that when ConfigurationWatchList exists, URLs are properly added.
     */
    @Test
    public void testAddToWatchListWithExistingCWL() throws MalformedURLException {
        // Create and register a ConfigurationWatchList
        ConfigurationWatchList cwl = new ConfigurationWatchList();
        cwl.setContext(context);
        ConfigurationWatchListUtil.registerConfigurationWatchList(context, cwl);
        
        URL url = new URL("file:///tmp/logback.xml");
        
        // Add to watch list
        ConfigurationWatchListUtil.addToWatchList(context, url, false);
        
        // URL should be in watch list
        ConfigurationWatchList retrievedCWL = ConfigurationWatchListUtil.getConfigurationWatchList(context);
        assertNotNull(retrievedCWL);
        assertTrue(retrievedCWL.hasAtLeastOneWatchableFile(), 
                "File should be added to existing watch list");
    }

    /**
     * Test the default addToWatchList method (no createCWL parameter).
     * This should behave like createCWL=false.
     */
    @Test
    public void testAddToWatchListDefault_NoMessage() throws MalformedURLException {
        URL url = new URL("file:///tmp/logback.xml");
        
        // Call addToWatchList without createCWL (defaults to false)
        ConfigurationWatchListUtil.addToWatchList(context, url);
        
        // No ConfigurationWatchList should be created
        assertNull(ConfigurationWatchListUtil.getConfigurationWatchList(context));
        
        // No spurious watch list messages should be logged
        assertFalse(statusChecker.containsMatch("ConfigurationWatchList not initialized"),
                "Should not log watch list message by default");
    }
}
