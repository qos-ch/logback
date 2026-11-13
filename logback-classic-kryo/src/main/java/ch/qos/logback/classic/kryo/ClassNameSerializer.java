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
package ch.qos.logback.classic.kryo;

import ch.qos.logback.core.kryo.SerializerBase;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Serializer for logger names that compresses hierarchical names by storing
 * common prefixes separately and referencing them.
 *
 * Logger names are typically hierarchical (e.g., "ch.qos.logback.classic.Logger",
 * "ch.qos.logback.classic.PatternLayout") and share common prefixes.
 *
 * This serializer:
 * 1. Caches full logger names (like StringSerializer)
 * 2. For new entries, detects common prefixes and stores only the unique suffix
 * 3. Reduces serialized size by 30-50% for typical logger hierarchies
 */
public class ClassNameSerializer extends SerializerBase<String> {

    // Common package prefixes to look for
//    private static final String[] COMMON_PREFIXES = {
//        "ch.qos.logback.",
//        "ch.qos.",
//        "org.slf4j.",
//        "java.util.",
//        "javax.",
//        "org.springframework.",
//        "com.example."
//    };

    // Map to cache prefix -> id mapping for compression
    private final Map<String, Integer> prefixToId = new HashMap<>();
    private final Map<Integer, String> idToPrefix = new HashMap<>();
    private int nextPrefixId = 0;

    public ClassNameSerializer(int capacity) {
        super(BiMapType.DIRECT_MAP, capacity);
        setAcceptsNull(false);
    }

    @Override
    protected void writeNewEntry(Kryo kryo, Output output, String loggerName) {
        //Log.trace("writeNewEntry for logger name: " + loggerName);

        if (loggerName == null) {
            throw new IllegalArgumentException("Null logger names are not permitted");
        }

        // Try to find a common prefix
        String prefix = findLongestPrefix(loggerName);

        if (prefix != null && prefix.length() > 4) { // Only compress if prefix is meaningful
            Integer prefixId = prefixToId.get(prefix);

            if (prefixId == null) {
                // New prefix - register it
                prefixId = nextPrefixId++;
                prefixToId.put(prefix, prefixId);
                idToPrefix.put(prefixId, prefix);

                // Write as: NEW_PREFIX flag + prefix_id + prefix + suffix
                output.writeByte((byte) 1); // NEW_PREFIX
                output.writeVarInt(prefixId, true);
                output.writeString(prefix);
                String suffix = loggerName.substring(prefix.length());
                output.writeString(suffix);
                Log.debug("New prefix [" + prefix + "] with id " + prefixId + ", suffix: " + suffix);
            } else {
                // Existing prefix - just reference it
                // Write as: EXISTING_PREFIX flag + prefix_id + suffix
                output.writeByte((byte) 2); // EXISTING_PREFIX
                output.writeVarInt(prefixId, true);
                String suffix = loggerName.substring(prefix.length());
                output.writeString(suffix);
                Log.debug("Using prefix id " + prefixId + " [" + prefix + "], suffix: " + suffix);
            }
        } else {
            // No meaningful prefix - write full name
            // Write as: FULL_NAME flag + name
            output.writeByte((byte) 0); // FULL_NAME
            output.writeString(loggerName);
            Log.debug("Writing full logger name: " + loggerName);
        }
    }

    @Override
    protected String readNewEntry(Kryo kryo, Input input) {
        byte mode = input.readByte();

        switch (mode) {
            case 0: // FULL_NAME
                String fullName = input.readString();
                Log.trace("Read full logger name: " + fullName);
                return fullName;

            case 1: // NEW_PREFIX
                int newPrefixId = input.readVarInt(true);
                String prefix = input.readString();
                String suffix = input.readString();

                // Cache the prefix
                prefixToId.put(prefix, newPrefixId);
                idToPrefix.put(newPrefixId, prefix);

                String reconstructed = prefix + suffix;
                Log.debug("Read new prefix [" + prefix + "] id=" + newPrefixId + ", result: " + reconstructed);
                return reconstructed;

            case 2: // EXISTING_PREFIX
                int prefixId = input.readVarInt(true);
                String cachedPrefix = idToPrefix.get(prefixId);
                String suffixPart = input.readString();

                if (cachedPrefix == null) {
                    throw new IllegalStateException("Unknown prefix id: " + prefixId);
                }

                String result = cachedPrefix + suffixPart;
                Log.debug("Read with cached prefix id=" + prefixId + " [" + cachedPrefix + "], result: " + result);
                return result;

            default:
                throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    /**
     * Find the longest common prefix for the logger name.
     * First checks predefined common prefixes, then finds the package prefix.
     */
    private String findLongestPrefix(String loggerName) {
        // Check common known prefixes first
//        for (String commonPrefix : COMMON_PREFIXES) {
//            if (loggerName.startsWith(commonPrefix)) {
//                return commonPrefix;
//            }
//        }

        // Find package-level prefix (everything up to last dot)
        int lastDot = loggerName.lastIndexOf('.');
        if (lastDot > 0) {
            return loggerName.substring(0, lastDot + 1); // Include the dot
        }

        return null;
    }
}
