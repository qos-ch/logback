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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.helpers.BasicMarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fixed list of events, identical on every run, and the streams the encoder wrote for it
 * ({@code golden/*.kryo} on the test class path). Encoding these events must reproduce those
 * bytes exactly: a difference means the wire format changed.
 *
 * <p>Nothing depends on the clock, the call stack or the machine: timestamps, stack traces
 * and caller data are spelled out. The events repeat logger names, caller frames, MDC maps
 * and markers so that the codec caches see hits as well as new entries.
 */
public final class GoldenEvents {

    /** Folder of the golden streams, on the test class path. */
    public static final String RESOURCE_FOLDER = "golden/";

    /** Where the golden streams live in the source tree. */
    public static final String SOURCE_FOLDER = "src/test/resources/" + RESOURCE_FOLDER;

    /** Set to {@code true} to rewrite the golden streams instead of checking them. */
    public static final String WRITE_PROPERTY = "logback.kryo.golden.write";

    static final long T0 = 1_767_225_600_000L; // 2026-01-01T00:00:00Z

    private GoldenEvents() {
    }

    public static String fileName(EncoderVersionTag tag) {
        return "golden" + tag.toSuffix();
    }

    public static byte[] readGolden(EncoderVersionTag tag) {
        String name = RESOURCE_FOLDER + fileName(tag);
        try (InputStream in = GoldenEvents.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException("missing golden stream " + name);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<LoggingEventKryoVO> events() {
        LoggerContextVO context = loggerContext();
        BasicMarkerFactory markerFactory = new BasicMarkerFactory();
        Marker audit = markerFactory.getMarker("AUDIT");
        Marker slow = markerFactory.getMarker("SLOW");

        StackTraceElement[] invoiceCaller = {
                new StackTraceElement("app", null, null, "com.example.billing.InvoiceService", "issue",
                        "InvoiceService.java", 87),
                new StackTraceElement("app", null, null, "com.example.billing.InvoiceController", "post",
                        "InvoiceController.java", 41),
        };
        StackTraceElement[] orderCaller = {
                new StackTraceElement(null, null, null, "com.example.orders.OrderService", "place",
                        null, -1),
                new StackTraceElement(null, "java.base", "17.0.9", "java.lang.Thread", "run",
                        "Thread.java", 840),
        };

        List<LoggingEventKryoVO> events = new ArrayList<>();
        long seq = 1000;
        for (int round = 0; round < 3; round++) {
            long t = T0 + round * 60_000L;

            events.add(builder(context, seq++, t + 1, round)
                    .withThreadName("main")
                    .withLoggerName("com.example.billing.InvoiceService")
                    .withLevelInt(Level.INFO_INT)
                    .withMessage("Invoice {} issued to {} for {} EUR")
                    .withArgumentArray(new Object[]{1_000 + round, "ACME Corp", 129.5d + round})
                    .withMdcPropertyMap(mdc("requestId", "req-" + (round % 2), "userId", "u-17"))
                    .withCallerData(invoiceCaller)
                    .build());

            events.add(builder(context, seq++, t + 2, round)
                    .withThreadName("http-nio-8080-exec-" + (round + 1))
                    .withLoggerName("com.example.billing.InvoiceController")
                    .withLevelInt(Level.DEBUG_INT)
                    .withMessage("Request took {} ms (limit {}), retries {}")
                    .withArgumentArray(new Object[]{37L + round, 25, null, (short) 3, 0.5f})
                    .withMarkerList(markers(slow))
                    .withKeyValuePairList(kvps(new KeyValuePair("path", "/invoices"),
                            new KeyValuePair("status", 201), new KeyValuePair("note", null)))
                    .withCallerData(invoiceCaller)
                    .build());

            events.add(builder(context, seq++, t + 3, round)
                    .withThreadName("orders-worker")
                    .withLoggerName("com.example.orders.OrderService")
                    .withLevelInt(Level.WARN_INT)
                    .withMessage("Stock low for item {}")
                    .withArgumentArray(new Object[]{"SKU-" + round})
                    .withMarkerList(markers(audit, slow))
                    .withMdcPropertyMap(mdc("requestId", "req-" + (round % 2), "userId", "u-17"))
                    .withKeyValuePairList(kvps(new KeyValuePair("warehouse", "ZRH-1")))
                    .withCallerData(orderCaller)
                    .build());

            events.add(builder(context, seq++, t + 4, round)
                    .withThreadName("orders-worker")
                    .withLoggerName("com.example.orders.OrderService")
                    .withLevelInt(Level.ERROR_INT)
                    .withMessage("Order placement failed")
                    .withThrowableProxy(throwable(round))
                    .withMdcPropertyMap(mdc("orderId", "o-" + round))
                    .build());

            events.add(builder(context, seq++, t + 5, round)
                    .withThreadName("main")
                    .withLoggerName("Root")
                    .withLevelInt(Level.TRACE_INT)
                    .withMessage("unicode éè中文 😀 and no arguments")
                    .build());
        }
        return events;
    }

    private static LoggingEventKryoVO.Builder builder(LoggerContextVO context, long seq, long timestamp,
                                                      int round) {
        return new LoggingEventKryoVO.Builder()
                .withLoggerContextVO(context)
                .withSequenceNumber(seq)
                .withTimestamp(timestamp)
                .withNanoseconds((int) ((timestamp % 1000) * 1_000_000) + 1234 * (round + 1));
    }

    private static LoggerContextVO loggerContext() {
        Map<String, String> properties = new HashMap<>();
        properties.put("HOSTNAME", "golden-host");
        return new LoggerContextVO("golden", properties, T0 - 3_600_000L);
    }

    /** Logback's MDC snapshot: an unmodifiable view of a {@link HashMap}. */
    private static Map<String, String> mdc(String... keysAndValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    private static List<Marker> markers(Marker... markers) {
        List<Marker> list = new ArrayList<>();
        Collections.addAll(list, markers);
        return list;
    }

    private static List<KeyValuePair> kvps(KeyValuePair... pairs) {
        List<KeyValuePair> list = new ArrayList<>();
        Collections.addAll(list, pairs);
        return list;
    }

    /** An exception with a cause and a suppressed exception, all with fixed stack traces. */
    private static ThrowableProxyVO throwable(int round) {
        StackTraceElement repository = new StackTraceElement("app", null, null,
                "com.example.orders.OrderRepository", "save", "OrderRepository.java", 120);
        StackTraceElement service = new StackTraceElement("app", null, null,
                "com.example.orders.OrderService", "place", "OrderService.java", 64);
        StackTraceElement thread = new StackTraceElement(null, "java.base", "17.0.9",
                "java.lang.Thread", "run", "Thread.java", 840);
        StackTraceElement socket = new StackTraceElement(null, "java.base", "17.0.9",
                "sun.nio.ch.SocketDispatcher", "read0", null, -2);

        IllegalStateException cause = new IllegalStateException("connection pool exhausted");
        cause.setStackTrace(new StackTraceElement[]{socket, repository, service, thread});

        java.io.IOException suppressed = new java.io.IOException("rollback failed");
        suppressed.setStackTrace(new StackTraceElement[]{repository, service, thread});

        RuntimeException top = new RuntimeException("cannot place order o-" + round, cause);
        top.setStackTrace(new StackTraceElement[]{service, thread});
        top.addSuppressed(suppressed);

        return ThrowableProxyVO.build(new ThrowableProxy(top));
    }
}
