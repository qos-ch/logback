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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.joran.spi.JoranException;

import java.io.File;
import java.io.IOException;

import static ch.qos.logback.core.kryo.KryoCoreConstants.KRYO_UNSAFE_PROPERTY_KEY;

public class KryoDecoderApp {

    public static void main(String[] args) throws IOException, JoranException {
        System.setProperty(KRYO_UNSAFE_PROPERTY_KEY, "false");

        if (args.length != 2) {
            usage();
            System.exit(1);
        }

        System.out.println("In KryoDecoderApp main()");
        LoggerContext loggerContext = (LoggerContext) new LoggerContext();
        LogbackMDCAdapter logbackMDCAdapter = new LogbackMDCAdapter();
        loggerContext.setMDCAdapter(logbackMDCAdapter);
        loggerContext.setName("KryoDecoderApp");

        String filename = args[0];
        String configFile = args[1];

        configureLoggerContext(loggerContext, configFile);

        KryoDecoder decoder = new KryoDecoder(new File(filename));
        KryoDecoder.DecodeStats stats = decoder.decodeAndAppend(loggerContext);

        System.out.println("Decoded " + stats.count() + " logging events from file: " + filename
                + " (version=" + (decoder.getStreamVersion() & 0xFF)
                + ", serializer=" + decoder.getSerializerType()
                + ", decodeNanos=" + stats.decodeNanos()
                + ", consumeNanos=" + stats.consumeNanos() + ")");
    }

    private static void configureLoggerContext(LoggerContext loggerContext, String configFile) throws JoranException {
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(configFile);
    }

    private static void usage() {
        System.err.println("Usage: java KryoDecoderApp <filename> <configFile>");
        System.err.println("  <filename>   The name of the file to decode (must include Kryo stream header)");
        System.err.println("  <configFile> The logback configuration file");
        System.err.println("Serializer type and stream version are read from the file header.");
    }
}
