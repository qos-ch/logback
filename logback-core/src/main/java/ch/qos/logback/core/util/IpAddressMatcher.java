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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Matches an {@link InetAddress} against a single IP address or a CIDR network
 * range.
 * <p>
 * Accepted forms:
 * <ul>
 * <li>Single address: {@code 192.168.1.10}, {@code 2001:db8::1}</li>
 * <li>CIDR range: {@code 192.168.1.0/24}, {@code 2001:db8::/32}</li>
 * </ul>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @since 1.6.2
 */
public class IpAddressMatcher {

    private final String original;
    private final byte[] networkAddress;
    private final int prefixLength;

    /**
     * Creates a matcher for the given address or CIDR specification.
     *
     * @param addressOrCidr a single IP or {@code address/prefixLength}
     * @throws IllegalArgumentException if the specification is invalid
     */
    public IpAddressMatcher(String addressOrCidr) {
        if (addressOrCidr == null) {
            throw new IllegalArgumentException("addressOrCidr must not be null");
        }
        this.original = addressOrCidr.trim();
        if (this.original.isEmpty()) {
            throw new IllegalArgumentException("addressOrCidr must not be empty");
        }

        String addressPart;
        Integer explicitPrefix = null;
        int slashIndex = this.original.indexOf('/');
        if (slashIndex >= 0) {
            addressPart = this.original.substring(0, slashIndex).trim();
            String prefixPart = this.original.substring(slashIndex + 1).trim();
            if (addressPart.isEmpty() || prefixPart.isEmpty()) {
                throw new IllegalArgumentException("Invalid CIDR specification [" + addressOrCidr + "]");
            }
            try {
                explicitPrefix = Integer.parseInt(prefixPart);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid prefix length in [" + addressOrCidr + "]", e);
            }
        } else {
            addressPart = this.original;
        }

        InetAddress parsed;
        try {
            parsed = InetAddress.getByName(addressPart);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host or invalid IP address [" + addressPart + "]", e);
        }

        this.networkAddress = parsed.getAddress();
        int maxPrefix = this.networkAddress.length * 8;
        if (explicitPrefix != null) {
            if (explicitPrefix < 0 || explicitPrefix > maxPrefix) {
                throw new IllegalArgumentException("Prefix length " + explicitPrefix
                        + " is out of range for address [" + addressPart + "] (0-" + maxPrefix + ")");
            }
            this.prefixLength = explicitPrefix;
        } else {
            this.prefixLength = maxPrefix;
        }
    }

    /**
     * Returns {@code true} if the given address matches this matcher.
     *
     * @param address the address to test; may be {@code null}
     * @return {@code true} if {@code address} matches
     */
    public boolean matches(InetAddress address) {
        if (address == null) {
            return false;
        }
        return matchesBytes(address.getAddress());
    }

    /**
     * Returns {@code true} if the given address string matches this matcher.
     *
     * @param address an IP address string
     * @return {@code true} if the address matches
     * @throws IllegalArgumentException if {@code address} cannot be parsed
     */
    public boolean matches(String address) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        try {
            return matches(InetAddress.getByName(address.trim()));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host or invalid IP address [" + address + "]", e);
        }
    }

    private boolean matchesBytes(byte[] candidate) {
        if (candidate == null || candidate.length != networkAddress.length) {
            return false;
        }

        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (candidate[i] != networkAddress[i]) {
                return false;
            }
        }

        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits);
            if ((candidate[fullBytes] & mask) != (networkAddress[fullBytes] & mask)) {
                return false;
            }
        }

        return true;
    }

    /**
     * The original specification used to construct this matcher.
     */
    public String getSpecification() {
        return original;
    }

    @Override
    public String toString() {
        return "IpAddressMatcher[" + original + "]";
    }
}
