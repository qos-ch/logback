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

import java.io.File;
import java.io.IOException;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;

/**
 * Decode every event in a Kryo file and print the count and elapsed time.
 */
public class KyroDecodeCountApp {

    public static void main(String[] args) throws IOException {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");

        if (args.length != 1) {
            usage();
            System.exit(1);
        }

        File file = new File(args[0]);
        if (!file.isFile()) {
            System.err.println("Not a file: " + file.getAbsolutePath());
            System.exit(1);
        }

        long t0 = System.nanoTime();
        KryoDecoder decoder = new KryoDecoder(file);
        KryoDecoder.DecodeStats stats = decoder.decodeWithConsumer(event -> {
        });
        long ns = System.nanoTime() - t0;

        System.out.println("Decoded " + formatCount(stats.count()) + " events from " + file.getAbsolutePath()
                + " in " + formatDuration(ns)
                + " (version=" + (decoder.getStreamVersion() & 0xFF)
                + ", serializer=" + decoder.getSerializerType() + ")");

        System.out.println("Average decode time: " + formatDuration(ns / stats.count()));

        long fileSize = file.length();
        System.out.println("File size: " + formatFileSize(fileSize) + " bytes, average event size: "
                + (fileSize / (double) stats.count()) + " bytes/event"
        );

        System.out.println();
    }

    static String formatCount(long count) {
        if (count >= 1_000_000_000L) {
            return String.format("%.3fB", count / 1_000_000_000.0);
        }
        if (count >= 1_000_000L) {
            return String.format("%.3fM", count / 1_000_000.0);
        }
        if (count >= 1_000L) {
            return String.format("%.3fK", count / 1_000.0);
        }
        return Long.toString(count);
    }

    static String formatFileSize(long size) {
        if (size >= 1_000_000_000L) {
            return String.format("%.3f GB", size / 1_000_000_000.0);
        }
        if (size >= 1_000_000L) {
            return String.format("%.3f MB", size / 1_000_000.0);
        }
        if (size >= 1_000L) {
            return String.format("%.3f KB", size / 1_000.0);
        }
        return Long.toString(size) + " B";
    }

    static String formatDuration(long ns) {
        if (ns >= 1_000_000_000L) {
            return String.format("%.3f s", ns / 1_000_000_000.0);
        }
        if (ns >= 1_000_000L) {
            return String.format("%.3f ms", ns / 1_000_000.0);
        }
        return String.format("%.3f µs", ns / 1_000.0);
    }

    private static void usage() {
        System.err.println("Usage: java " + KyroDecodeCountApp.class.getName() + " <filename>");
        System.err.println("  <filename>  Kryo stream (header STWD + version + flags)");
    }
}
