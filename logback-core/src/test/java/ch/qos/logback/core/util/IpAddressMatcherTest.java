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
package ch.qos.logback.core.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

public class IpAddressMatcherTest {

    @Test
    public void singleIPv4AddressMatchesExactly() throws UnknownHostException {
        IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.10");
        assertTrue(matcher.matches("192.168.1.10"));
        assertTrue(matcher.matches(InetAddress.getByName("192.168.1.10")));
        assertFalse(matcher.matches("192.168.1.11"));
        assertFalse(matcher.matches("10.0.0.1"));
    }

    @Test
    public void ipv4CidrSlash24() {
        IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.0/24");
        assertTrue(matcher.matches("192.168.1.0"));
        assertTrue(matcher.matches("192.168.1.1"));
        assertTrue(matcher.matches("192.168.1.255"));
        assertFalse(matcher.matches("192.168.0.1"));
        assertFalse(matcher.matches("192.168.2.1"));
        assertFalse(matcher.matches("10.0.0.1"));
    }

    @Test
    public void ipv4CidrSlash16() {
        IpAddressMatcher matcher = new IpAddressMatcher("10.0.0.0/16");
        assertTrue(matcher.matches("10.0.0.1"));
        assertTrue(matcher.matches("10.0.255.255"));
        assertFalse(matcher.matches("10.1.0.1"));
        assertFalse(matcher.matches("11.0.0.1"));
    }

    @Test
    public void ipv4CidrSlash32IsSingleHost() {
        IpAddressMatcher matcher = new IpAddressMatcher("203.0.113.5/32");
        assertTrue(matcher.matches("203.0.113.5"));
        assertFalse(matcher.matches("203.0.113.6"));
    }

    @Test
    public void ipv4CidrSlash0MatchesAll() {
        IpAddressMatcher matcher = new IpAddressMatcher("0.0.0.0/0");
        assertTrue(matcher.matches("1.2.3.4"));
        assertTrue(matcher.matches("255.255.255.255"));
    }

    @Test
    public void nonOctetAlignedPrefix() {
        // /25: first half of 192.168.1.0/24
        IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.0/25");
        assertTrue(matcher.matches("192.168.1.0"));
        assertTrue(matcher.matches("192.168.1.127"));
        assertFalse(matcher.matches("192.168.1.128"));
        assertFalse(matcher.matches("192.168.1.255"));
    }

    @Test
    public void ipv6SingleAddress() throws UnknownHostException {
        IpAddressMatcher matcher = new IpAddressMatcher("2001:db8::1");
        assertTrue(matcher.matches("2001:db8::1"));
        assertTrue(matcher.matches(InetAddress.getByName("2001:db8:0:0:0:0:0:1")));
        assertFalse(matcher.matches("2001:db8::2"));
    }

    @Test
    public void ipv6Cidr() {
        IpAddressMatcher matcher = new IpAddressMatcher("2001:db8::/32");
        assertTrue(matcher.matches("2001:db8::1"));
        assertTrue(matcher.matches("2001:db8:ffff::1"));
        assertFalse(matcher.matches("2001:db9::1"));
        assertFalse(matcher.matches("2001:db7::1"));
    }

    @Test
    public void differentAddressFamilyDoesNotMatch() {
        IpAddressMatcher v4 = new IpAddressMatcher("192.168.1.0/24");
        assertFalse(v4.matches("2001:db8::1"));

        IpAddressMatcher v6 = new IpAddressMatcher("2001:db8::/32");
        assertFalse(v6.matches("192.168.1.1"));
    }

    @Test
    public void nullAddressDoesNotMatch() {
        IpAddressMatcher matcher = new IpAddressMatcher("127.0.0.1");
        assertFalse(matcher.matches((InetAddress) null));
    }

    @Test
    public void invalidSpecificationsThrow() {
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher(null));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher(""));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("   "));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("192.168.1.0/"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("/24"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("192.168.1.0/abc"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("192.168.1.0/33"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("192.168.1.0/-1"));
        assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("not-an-ip"));
    }

    @Test
    public void whitespaceAroundSpecificationIsTolerated() {
        IpAddressMatcher matcher = new IpAddressMatcher("  192.168.1.0/24  ");
        assertTrue(matcher.matches("192.168.1.42"));
    }
}
