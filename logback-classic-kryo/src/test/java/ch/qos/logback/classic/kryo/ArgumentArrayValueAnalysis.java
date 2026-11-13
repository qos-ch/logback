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

import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import org.slf4j.event.KeyValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;

/**
 * Decode a Kryo stream and report redundancy in {@code argumentArray} <em>values</em>
 * (not array identity) to inform V5 encoding.
 *
 * <p>Current V4/V5 payload for a present array is {@code varInt(length)} then per
 * element {@code SafeObjectCodec} (1-byte type tag + nested string/number encoding).
 * V5 only folds the null flag into the presence bitmap; the payload is unchanged.
 */
public class ArgumentArrayValueAnalysis {

    /** Same threshold as {@link MultiEventCompressedStringSerializer}. */
    static final int DEFLATE_THRESHOLD = 256;

    public static void main(String[] args) throws Exception {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");
        Log.ERROR();
        if (args.length != 1) {
            System.err.println("Usage: java " + ArgumentArrayValueAnalysis.class.getName() + " <kryo-file>");
            System.exit(1);
        }
        File file = new File(args[0]);
        Holder h = new Holder();
        KryoDecoder decoder = new KryoDecoder(file);
        KryoDecoder.DecodeStats stats = decoder.decodeWithConsumer(event -> consume(h, event));
        printReport(file, decoder, stats, h);
    }

    static void consume(Holder h, LoggingEventKryoVO event) {
        h.total++;
        String message = event.getMessage();
        if (message != null) {
            h.messages.merge(message, 1, Integer::sum);
        }
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null) {
            for (String v : mdc.values()) {
                if (v != null) {
                    h.mdcValues.merge(v, 1, Integer::sum);
                }
            }
        }
        List<KeyValuePair> kvps = event.getKeyValuePairs();
        if (kvps != null) {
            for (KeyValuePair kvp : kvps) {
                if (kvp != null && kvp.value instanceof String) {
                    h.kvpValues.merge((String) kvp.value, 1, Integer::sum);
                }
            }
        }

        Object[] arguments = event.getArgumentArray();
        if (arguments == null) {
            h.nullArgs++;
            return;
        }
        h.presentArgs++;
        bump(h.arityHist, arguments.length);
        if (arguments.length == 0) {
            h.emptyArgs++;
            return;
        }

        int placeholders = placeholderCount(message);
        if (placeholders == arguments.length) {
            h.arityMatchesPlaceholders++;
        } else {
            h.arityMismatchPlaceholders++;
        }

        boolean allStrings = true;
        for (Object arg : arguments) {
            h.slots++;
            String type = typeName(arg);
            h.types.merge(type, 1, Integer::sum);
            if (arg == null) {
                allStrings = false;
                continue;
            }
            if (!(arg instanceof String)) {
                allStrings = false;
                h.nonStringSlots++;
                continue;
            }
            String str = (String) arg;
            h.stringSlots++;
            bump(h.stringLenHist, str.length());
            if (str.length() >= DEFLATE_THRESHOLD) {
                h.longStringSlots++;
            }
            ValueStats vs = h.values.get(str);
            if (vs == null) {
                vs = new ValueStats();
                vs.firstEvent = h.total - 1;
                vs.chars = str.length();
                vs.sample = str;
                h.values.put(str, vs);
            }
            vs.count++;
            vs.lastEvent = h.total - 1;
        }
        if (allStrings) {
            h.allStringEvents++;
        }
    }

    static void printReport(File file, KryoDecoder decoder, KryoDecoder.DecodeStats stats, Holder h) {
        System.out.println("file: " + file.getAbsolutePath());
        System.out.println("decoded events: " + stats.count());
        System.out.println("stream version: " + (decoder.getStreamVersion() & 0xFF));
        System.out.println();

        Serializer<?> serializer = decoder.kryo.getSerializer(LoggingEventKryoVO.class);
        if (serializer instanceof FieldByteAccumulatorAware) {
            FieldByteAccumulatorAware aware = (FieldByteAccumulatorAware) serializer;
            Long argBytes = aware.getFieldByteAccumulator().snapshot().get("argumentArray");
            long totalBytes = aware.getFieldByteAccumulator().total();
            System.out.println("argumentArray field bytes: " + argBytes
                    + " (" + pct(argBytes == null ? 0 : argBytes.intValue(), (int) Math.min(totalBytes, Integer.MAX_VALUE))
                    + " of payload fields, payload total=" + totalBytes + ")");
            aware.getFieldByteAccumulator().dump(System.out);
            System.out.println();
        }

        System.out.println("--- presence / arity ---");
        System.out.println("null argumentArray:    " + h.nullArgs + " (" + pct(h.nullArgs, h.total) + ")");
        System.out.println("present argumentArray: " + h.presentArgs + " (" + pct(h.presentArgs, h.total) + ")");
        System.out.println("empty (length 0):      " + h.emptyArgs);
        System.out.println("slots:                 " + h.slots);
        if (h.presentArgs > 0) {
            System.out.printf("average arity (present): %.2f%n", h.slots / (double) h.presentArgs);
        }
        dumpHist("arity (length -> events):", h.arityHist, 20);
        int arity1 = h.arityHist.getOrDefault(1, 0);
        System.out.println("arity==1 among present: " + arity1 + " / " + h.presentArgs
                + " (" + pct(arity1, h.presentArgs) + ")");
        System.out.println("arity matches '{}' placeholders: " + h.arityMatchesPlaceholders
                + ", mismatches: " + h.arityMismatchPlaceholders);
        System.out.println();

        System.out.println("--- element types ---");
        h.types.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %s -> %d (%s of slots)%n",
                        e.getKey(), e.getValue(), pct(e.getValue(), h.slots)));
        System.out.println("events whose args are all String: " + h.allStringEvents
                + " / " + (h.presentArgs - h.emptyArgs)
                + " (" + pct(h.allStringEvents, h.presentArgs - h.emptyArgs) + ")");
        System.out.println();

        printValueSection(h);
        printLengthSection(h);
        printOverlapSection(h);
        printEncodingImplications(h);
    }

    static void printValueSection(Holder h) {
        System.out.println("--- values (equals) ---");
        System.out.println("distinct string values: " + h.values.size());
        System.out.println("string slots:           " + h.stringSlots);

        List<Integer> counts = new ArrayList<>();
        int singletons = 0;
        int singletonSlots = 0;
        int repeatedValues = 0;
        int repeatedSlots = 0;
        int max = 0;
        ValueStats maxVs = null;
        Map<Integer, Integer> freqHist = new TreeMap<>();
        for (ValueStats vs : h.values.values()) {
            counts.add(vs.count);
            bump(freqHist, vs.count);
            if (vs.count == 1) {
                singletons++;
                singletonSlots++;
            } else {
                repeatedValues++;
                repeatedSlots += vs.count;
            }
            if (vs.count > max) {
                max = vs.count;
                maxVs = vs;
            }
        }
        Collections.sort(counts);
        int hitsIfDirectMap = Math.max(0, h.stringSlots - h.values.size());
        System.out.println("values that appear once:     " + singletons
                + " covering " + singletonSlots + " slots (" + pct(singletonSlots, h.stringSlots) + ")");
        System.out.println("values that appear 2+ times: " + repeatedValues
                + " covering " + repeatedSlots + " slots (" + pct(repeatedSlots, h.stringSlots) + ")");
        System.out.println("DIRECT_MAP string-id hits (first sighting is a miss): "
                + hitsIfDirectMap + " / " + h.stringSlots
                + " (" + pct(hitsIfDirectMap, h.stringSlots) + ")");
        System.out.println("FREQUENCY_GATED: first sighting unregistered; "
                + singletons + " values would never occupy an LRU slot");
        System.out.println("max frequency: " + max + (maxVs == null ? "" : "  sample=" + truncate(maxVs.sample, 80)));
        System.out.println("frequency percentiles:"
                + " p50=" + percentile(counts, 50)
                + " p75=" + percentile(counts, 75)
                + " p90=" + percentile(counts, 90)
                + " p95=" + percentile(counts, 95)
                + " p99=" + percentile(counts, 99)
                + " max=" + last(counts));
        System.out.println();
        dumpHist("value frequency (count -> number of distinct values):", freqHist, 30);
        System.out.println();
        System.out.println("top values:");
        h.values.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().count, a.getValue().count))
                .limit(15)
                .forEach(e -> {
                    ValueStats vs = e.getValue();
                    System.out.printf("  %4d slots  chars=%-4d  %s%n",
                            vs.count, vs.chars, truncate(vs.sample, 100));
                });
        System.out.println();
    }

    static void printLengthSection(Holder h) {
        System.out.println("--- string lengths ---");
        System.out.println("slots with length >= " + DEFLATE_THRESHOLD + " (deflate on NEW): "
                + h.longStringSlots + " / " + h.stringSlots
                + " (" + pct(h.longStringSlots, h.stringSlots) + ")");
        int longValues = 0;
        int longValueSlots = 0;
        for (ValueStats vs : h.values.values()) {
            if (vs.chars >= DEFLATE_THRESHOLD) {
                longValues++;
                longValueSlots += vs.count;
            }
        }
        System.out.println("distinct values >= " + DEFLATE_THRESHOLD + " chars: " + longValues
                + " covering " + longValueSlots + " slots");
        dumpHist("char length (length -> slots):", h.stringLenHist, 25);
        System.out.println();
    }

    static void printOverlapSection(Holder h) {
        int argAlsoMessage = 0;
        int argAlsoMessageSlots = 0;
        int argAlsoMdc = 0;
        int argAlsoMdcSlots = 0;
        int argAlsoKvp = 0;
        int argAlsoKvpSlots = 0;
        for (Map.Entry<String, ValueStats> e : h.values.entrySet()) {
            String v = e.getKey();
            int slots = e.getValue().count;
            if (h.messages.containsKey(v)) {
                argAlsoMessage++;
                argAlsoMessageSlots += slots;
            }
            if (h.mdcValues.containsKey(v)) {
                argAlsoMdc++;
                argAlsoMdcSlots += slots;
            }
            if (h.kvpValues.containsKey(v)) {
                argAlsoKvp++;
                argAlsoKvpSlots += slots;
            }
        }
        System.out.println("--- overlap with other fields (equals) ---");
        System.out.println("distinct messages:    " + h.messages.size());
        System.out.println("distinct MDC values:  " + h.mdcValues.size());
        System.out.println("distinct KVP values:  " + h.kvpValues.size());
        System.out.println("arg values that also appear as a message: "
                + argAlsoMessage + " values, " + argAlsoMessageSlots + " arg slots ("
                + pct(argAlsoMessageSlots, h.stringSlots) + ")");
        System.out.println("arg values that also appear as an MDC value: "
                + argAlsoMdc + " values, " + argAlsoMdcSlots + " arg slots ("
                + pct(argAlsoMdcSlots, h.stringSlots) + ")");
        System.out.println("arg values that also appear as a KVP value: "
                + argAlsoKvp + " values, " + argAlsoKvpSlots + " arg slots ("
                + pct(argAlsoKvpSlots, h.stringSlots) + ")");
        System.out.println();
        System.out.println("arg values that are also MDC values:");
        h.values.entrySet().stream()
                .filter(e -> h.mdcValues.containsKey(e.getKey()))
                .sorted((a, b) -> Integer.compare(b.getValue().count, a.getValue().count))
                .limit(10)
                .forEach(e -> System.out.printf("  arg=%d  mdc=%d  %s%n",
                        e.getValue().count,
                        h.mdcValues.get(e.getKey()),
                        truncate(e.getKey(), 80)));
        System.out.println();
    }

    static void printEncodingImplications(Holder h) {
        int presentNonEmpty = h.presentArgs - h.emptyArgs;
        int varIntPlusTag = presentNonEmpty + h.slots;
        System.out.println("--- encoding implications (this dump) ---");
        System.out.println("V5 already: 1-bit presence; payload still varInt(length)+tag+value.");
        System.out.println("If every present array is arity 1, varInt(length) is "
                + presentNonEmpty + " bytes that a 2-bit packed argc in the presence byte could drop.");
        System.out.println("If every slot is String, type tags are "
                + h.stringSlots + " bytes that an all-string / omitted-tag encoding could drop.");
        System.out.println("Combined structural overhead (varInt+tag) ~ " + varIntPlusTag
                + " bytes vs nested string headers+payloads.");
        System.out.println("Whole-array identity cache would not hit (new Object[] per event/decode).");
        System.out.println("Whole-array equals cache would mostly duplicate the nested string cache");
        System.out.println("when arity is 1 and the element is a String.");
    }

    static int placeholderCount(String message) {
        if (message == null) {
            return 0;
        }
        int n = 0;
        int from = 0;
        while (true) {
            int i = message.indexOf("{}", from);
            if (i < 0) {
                return n;
            }
            n++;
            from = i + 2;
        }
    }

    static String typeName(Object arg) {
        if (arg == null) {
            return "null";
        }
        return arg.getClass().getSimpleName();
    }

    static void bump(Map<Integer, Integer> hist, int key) {
        hist.merge(key, 1, Integer::sum);
    }

    static void dumpHist(String title, Map<Integer, Integer> hist, int maxRows) {
        System.out.println(title);
        if (hist.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        int rows = 0;
        for (Map.Entry<Integer, Integer> e : hist.entrySet()) {
            if (rows++ < maxRows) {
                System.out.printf("  %6d -> %6d%n", e.getKey(), e.getValue());
            }
        }
        if (rows > maxRows) {
            int rest = 0;
            int i = 0;
            for (int v : hist.values()) {
                if (i++ >= maxRows) {
                    rest += v;
                }
            }
            System.out.printf("  ... %d more buckets covering %d items%n", rows - maxRows, rest);
        }
    }

    static String pct(int n, int d) {
        if (d == 0) {
            return "n/a";
        }
        return String.format("%.1f%%", 100.0 * n / d);
    }

    static int percentile(List<Integer> sorted, int p) {
        if (sorted.isEmpty()) {
            return 0;
        }
        int i = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        if (i < 0) {
            i = 0;
        }
        if (i >= sorted.size()) {
            i = sorted.size() - 1;
        }
        return sorted.get(i);
    }

    static int last(List<Integer> sorted) {
        return sorted.isEmpty() ? 0 : sorted.get(sorted.size() - 1);
    }

    static String truncate(String s, int max) {
        if (s == null) {
            return "null";
        }
        String oneLine = s.replace('\n', ' ').replace('\r', ' ');
        if (oneLine.length() <= max) {
            return oneLine;
        }
        return oneLine.substring(0, max) + "...";
    }

    static final class Holder {
        int total;
        int nullArgs;
        int presentArgs;
        int emptyArgs;
        int slots;
        int stringSlots;
        int nonStringSlots;
        int longStringSlots;
        int allStringEvents;
        int arityMatchesPlaceholders;
        int arityMismatchPlaceholders;
        final Map<Integer, Integer> arityHist = new TreeMap<>();
        final Map<Integer, Integer> stringLenHist = new TreeMap<>();
        final Map<String, Integer> types = new TreeMap<>();
        final Map<String, ValueStats> values = new LinkedHashMap<>();
        final Map<String, Integer> messages = new LinkedHashMap<>();
        final Map<String, Integer> mdcValues = new LinkedHashMap<>();
        final Map<String, Integer> kvpValues = new LinkedHashMap<>();
    }

    static final class ValueStats {
        int count;
        int firstEvent;
        int lastEvent;
        int chars;
        String sample;
    }
}
