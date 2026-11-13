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

import ch.qos.logback.classic.kryo.KryoDecoder;
import ch.qos.logback.classic.kryo.LoggingEventKryoVO;
import com.esotericsoftware.kryo.kryo5.minlog.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Decode a V4 Kryo stream and report how often the same MDC map instance
 * (identity, as returned by the identity-cached serializer) is reused.
 *
 * A cache hit on write is decoded as the same Java object, so identity reuse
 * here is a lower bound on writer-side reuse of the MDC map instance (RESET
 * splits one original instance into multiple decoded instances).
 */
public class MdcIdentityReuse {

    public static void main(String[] args) throws Exception {
        Log.ERROR();
        File file = new File(args[0]);
        Holder h = new Holder();
        KryoDecoder decoder = new KryoDecoder(file);
        KryoDecoder.DecodeStats stats = decoder.decodeWithConsumer(event -> consume(h, event));

        finishRun(h);

        System.out.println("file: " + file.getAbsolutePath());
        System.out.println("decoded events: " + stats.count());
        System.out.println("stream version: " + (decoder.getStreamVersion() & 0xFF));
        System.out.println();
        System.out.println("total events seen: " + h.total);
        System.out.println("null MDC:          " + h.nullMdc);
        System.out.println("empty MDC:         " + h.emptyMdc);
        System.out.println("non-empty MDC:     " + h.withMdc);
        System.out.println("distinct MDC instances (identity): " + h.byIdentity.size());
        System.out.println("distinct MDC contents  (equals):   " + h.byEquals.size());
        System.out.println("consecutive identity runs: " + h.consecutiveRuns);
        System.out.println();

        Map<Integer, Integer> eventsPerInstHist = new TreeMap<>();
        int reusedInst = 0;
        int eventsOnReused = 0;
        int maxPerInst = 0;
        InstStats maxInst = null;
        List<Integer> perInstCounts = new ArrayList<>();
        for (InstStats s : h.byIdentity.values()) {
            bump(eventsPerInstHist, s.count);
            perInstCounts.add(s.count);
            if (s.count > 1) {
                reusedInst++;
                eventsOnReused += s.count;
            }
            if (s.count > maxPerInst) {
                maxPerInst = s.count;
                maxInst = s;
            }
        }

        System.out.println("instances used more than once: " + reusedInst
                + " / " + h.byIdentity.size()
                + " (" + pct(reusedInst, h.byIdentity.size()) + ")");
        System.out.println("events sitting on a reused instance: " + eventsOnReused
                + " / " + h.withMdc
                + " (" + pct(eventsOnReused, h.withMdc) + ")");
        System.out.println("max events per instance: " + maxPerInst);
        if (maxInst != null) {
            System.out.println("  firstEvent=" + maxInst.firstEvent
                    + " lastEvent=" + maxInst.lastEvent
                    + " size=" + maxInst.size
                    + " sample=" + maxInst.sample);
        }
        Collections.sort(perInstCounts);
        System.out.println("events-per-instance percentiles:"
                + " p50=" + percentile(perInstCounts, 50)
                + " p75=" + percentile(perInstCounts, 75)
                + " p90=" + percentile(perInstCounts, 90)
                + " p95=" + percentile(perInstCounts, 95)
                + " p99=" + percentile(perInstCounts, 99)
                + " max=" + (perInstCounts.isEmpty() ? 0 : perInstCounts.get(perInstCounts.size() - 1)));
        System.out.println();

        dumpHist("events per identity instance (count -> number of instances):", eventsPerInstHist, 40);
        System.out.println();
        dumpHist("consecutive identity run lengths (length -> number of runs):", h.runLengthHist, 40);
        System.out.println();

        List<Integer> runLengths = new ArrayList<>();
        int eventsInRunsGe10 = 0;
        int eventsInRunsGe50 = 0;
        int runsGe10 = 0;
        int runsGe50 = 0;
        for (Map.Entry<Integer, Integer> e : h.runLengthHist.entrySet()) {
            int len = e.getKey();
            int n = e.getValue();
            for (int i = 0; i < n; i++) {
                runLengths.add(len);
            }
            if (len >= 10) {
                runsGe10 += n;
                eventsInRunsGe10 += len * n;
            }
            if (len >= 50) {
                runsGe50 += n;
                eventsInRunsGe50 += len * n;
            }
        }
        Collections.sort(runLengths);
        System.out.println("run-length percentiles:"
                + " p50=" + percentile(runLengths, 50)
                + " p75=" + percentile(runLengths, 75)
                + " p90=" + percentile(runLengths, 90)
                + " p95=" + percentile(runLengths, 95)
                + " p99=" + percentile(runLengths, 99)
                + " max=" + (runLengths.isEmpty() ? 0 : runLengths.get(runLengths.size() - 1)));
        System.out.println("runs of length >= 10: " + runsGe10 + " covering " + eventsInRunsGe10
                + " events (" + pct(eventsInRunsGe10, h.withMdc) + " of MDC events)");
        System.out.println("runs of length >= 50: " + runsGe50 + " covering " + eventsInRunsGe50
                + " events (" + pct(eventsInRunsGe50, h.withMdc) + " of MDC events)");
        System.out.println();

        dumpHist("MDC map sizes (entry count -> number of events):", h.sizeHist, 20);
        System.out.println();
        System.out.println("MDC key frequencies (across events, identity-run first event only would undercount; counted per event):");
        h.keyFreq.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(20)
                .forEach(e -> System.out.printf("  %s -> %d%n", e.getKey(), e.getValue()));

        System.out.println();
        System.out.println("longest consecutive identity runs (startEvent, length, mapSize, sample):");
        h.longRuns.sort((a, b) -> Integer.compare(b.length, a.length));
        int shown = 0;
        for (Run r : h.longRuns) {
            if (shown++ >= 15) {
                break;
            }
            System.out.printf("  start=%d length=%d size=%d sample=%s%n",
                    r.start, r.length, r.size, r.sample);
        }

        System.out.println();
        System.out.println("top equals-identical MDC contents (events sharing equal maps, not necessarily same instance):");
        h.byEquals.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> System.out.printf("  %d events: %s%n", e.getValue(), truncate(e.getKey(), 180)));
    }

    static void consume(Holder h, LoggingEventKryoVO event) {
        int idx = h.total++;
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null) {
            h.nullMdc++;
            finishRun(h);
            h.prev = null;
            return;
        }
        if (mdc.isEmpty()) {
            h.emptyMdc++;
            finishRun(h);
            h.prev = null;
            return;
        }
        h.withMdc++;
        bump(h.sizeHist, mdc.size());
        for (String k : mdc.keySet()) {
            h.keyFreq.merge(k, 1, Integer::sum);
        }

        InstStats s = h.byIdentity.get(mdc);
        if (s == null) {
            s = new InstStats();
            s.firstEvent = idx;
            s.size = mdc.size();
            s.sample = truncate(String.valueOf(mdc), 160);
            h.byIdentity.put(mdc, s);
        }
        s.count++;
        s.lastEvent = idx;
        h.byEquals.merge(String.valueOf(mdc), 1, Integer::sum);

        if (h.prev != null && h.prev == mdc) {
            h.currentRun++;
        } else {
            finishRun(h);
            h.prev = mdc;
            h.currentRun = 1;
            h.firstEventOfRun = idx;
        }
    }

    static void finishRun(Holder h) {
        if (h.currentRun <= 0) {
            return;
        }
        h.consecutiveRuns++;
        bump(h.runLengthHist, h.currentRun);
        Run r = new Run();
        r.start = h.firstEventOfRun;
        r.length = h.currentRun;
        if (h.prev != null) {
            r.size = h.prev.size();
            r.sample = truncate(String.valueOf(h.prev), 160);
        }
        h.longRuns.add(r);
        h.currentRun = 0;
    }

    static void bump(Map<Integer, Integer> hist, int key) {
        hist.merge(key, 1, Integer::sum);
    }

    static void dumpHist(String title, Map<Integer, Integer> hist, int maxRows) {
        System.out.println(title);
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

    static String truncate(String s, int max) {
        if (s == null) {
            return "null";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }

    static final class Holder {
        int total;
        int withMdc;
        int nullMdc;
        int emptyMdc;
        int consecutiveRuns;
        int currentRun;
        int firstEventOfRun;
        Map<String, String> prev;
        final IdentityHashMap<Map<String, String>, InstStats> byIdentity = new IdentityHashMap<>();
        final Map<String, Integer> byEquals = new LinkedHashMap<>();
        final Map<Integer, Integer> runLengthHist = new TreeMap<>();
        final Map<Integer, Integer> sizeHist = new TreeMap<>();
        final Map<String, Integer> keyFreq = new TreeMap<>();
        final List<Run> longRuns = new ArrayList<>();
    }

    static final class InstStats {
        int count;
        int firstEvent;
        int lastEvent;
        int size;
        String sample;
    }

    static final class Run {
        int start;
        int length;
        int size;
        String sample;
    }
}
