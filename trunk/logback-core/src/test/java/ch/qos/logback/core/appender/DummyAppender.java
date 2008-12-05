/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.appender;

import java.io.Writer;

import ch.qos.logback.core.WriterAppender;

public class DummyAppender<E> extends WriterAppender<E> {

  DummyAppender(Writer writer) {
    this.setWriter(writer);
  }

}
