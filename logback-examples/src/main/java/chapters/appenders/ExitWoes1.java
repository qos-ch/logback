/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package chapters.appenders;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.WriterAppender;
import ch.qos.logback.core.encoder.EchoEncoder;

public class ExitWoes1 {

  public static void main(String[] args) throws Exception {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset(); // we want to override the default-config.
    WriterAppender<ILoggingEvent> writerAppender = new WriterAppender<ILoggingEvent>();
    writerAppender.setContext(lc);
    writerAppender.setEncoder(new EchoEncoder<ILoggingEvent>());

    OutputStream os = new FileOutputStream("exitWoes1.log");
    writerAppender.setOutputStream(os);
    writerAppender.start();
    Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
    root.addAppender(writerAppender);

    Logger logger = lc.getLogger(ExitWoes1.class);

    logger.debug("Hello world.");
  }
}