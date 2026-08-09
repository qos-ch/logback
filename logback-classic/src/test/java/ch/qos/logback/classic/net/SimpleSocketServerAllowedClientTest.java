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
package ch.qos.logback.classic.net;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class SimpleSocketServerAllowedClientTest {

    private SimpleSocketServer server;

    @BeforeEach
    public void setUp() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        server = new SimpleSocketServer(lc, 0);
    }

    @Test
    public void allClientsAllowedWhenNoRestrictionConfigured() throws Exception {
        assertTrue(server.isClientAllowed(InetAddress.getByName("203.0.113.1")));
        assertTrue(server.isClientAllowed(InetAddress.getByName("127.0.0.1")));
        assertTrue(server.isClientAllowed(null));
    }

    @Test
    public void singleAllowedAddress() throws Exception {
        server.addAllowedClientAddress("203.0.113.10");
        assertTrue(server.isClientAllowed(InetAddress.getByName("203.0.113.10")));
        assertFalse(server.isClientAllowed(InetAddress.getByName("203.0.113.11")));
        assertFalse(server.isClientAllowed(null));
    }

    @Test
    public void cidrRangeAllowed() throws Exception {
        server.addAllowedClientAddress("192.168.1.0/24");
        assertTrue(server.isClientAllowed(InetAddress.getByName("192.168.1.1")));
        assertTrue(server.isClientAllowed(InetAddress.getByName("192.168.1.254")));
        assertFalse(server.isClientAllowed(InetAddress.getByName("192.168.2.1")));
    }

    @Test
    public void multipleAllowedPatterns() throws Exception {
        server.addAllowedClientAddress("10.0.0.5");
        server.addAllowedClientAddress("192.168.0.0/16");
        assertTrue(server.isClientAllowed(InetAddress.getByName("10.0.0.5")));
        assertTrue(server.isClientAllowed(InetAddress.getByName("192.168.99.1")));
        assertFalse(server.isClientAllowed(InetAddress.getByName("10.0.0.6")));
        assertFalse(server.isClientAllowed(InetAddress.getByName("172.16.0.1")));
    }

    @Test
    public void setAllowedClientAddressesReplacesPrevious() throws Exception {
        server.addAllowedClientAddress("10.0.0.1");
        server.setAllowedClientAddresses(Arrays.asList("203.0.113.0/24"));
        assertFalse(server.isClientAllowed(InetAddress.getByName("10.0.0.1")));
        assertTrue(server.isClientAllowed(InetAddress.getByName("203.0.113.50")));
    }

    @Test
    public void setAllowedClientAddressesNullClearsRestriction() throws Exception {
        server.addAllowedClientAddress("10.0.0.1");
        server.setAllowedClientAddresses(null);
        assertTrue(server.isClientAllowed(InetAddress.getByName("203.0.113.1")));
    }

    @Test
    public void setAllowedClientAddressesEmptyClearsRestriction() throws Exception {
        server.addAllowedClientAddress("10.0.0.1");
        server.setAllowedClientAddresses(Collections.emptyList());
        assertTrue(server.isClientAllowed(InetAddress.getByName("203.0.113.1")));
    }

    @Test
    public void invalidAddressThrows() {
        assertThrows(IllegalArgumentException.class, () -> server.addAllowedClientAddress("not-valid"));
        assertThrows(IllegalArgumentException.class, () -> server.addAllowedClientAddress("1.2.3.4/99"));
    }
}
