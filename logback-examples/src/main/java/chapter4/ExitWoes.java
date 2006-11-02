/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package chapter4;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.WriterAppender;
import ch.qos.logback.core.layout.EchoLayout;



public class ExitWoes {

  public static void main(String[] args) throws Exception {
    LoggerContext lc = new LoggerContext();
    WriterAppender writerAppender = new WriterAppender();
    writerAppender.setContext(lc);
    writerAppender.setLayout(new EchoLayout());

    OutputStream os = new FileOutputStream("exitWoes1.log");
    writerAppender.setWriter(new OutputStreamWriter(os));
    writerAppender.setImmediateFlush(false);
    writerAppender.start();

    Logger logger = lc.getLogger(ExitWoes.class);

    logger.debug("Hello world.");
  }
}
