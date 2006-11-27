/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.access.net;

import java.io.IOException;
import java.io.OutputStream;

public class NOPOutputStream extends OutputStream {

  @Override
  public void write(int b) throws IOException {
    // do nothing

  }

}
