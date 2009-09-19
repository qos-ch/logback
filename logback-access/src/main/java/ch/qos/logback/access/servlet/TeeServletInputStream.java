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
package ch.qos.logback.access.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

class TeeServletInputStream extends ServletInputStream {

  InputStream in;
  byte[] inputBuffer;

  TeeServletInputStream(HttpServletRequest request) {
    duplicateInputStream(request);
  }

  @Override
  public int read() throws IOException {
    //System.out.println("zzzzzzzzzz TeeServletInputStream.read called");
    return in.read();
  }

  private void duplicateInputStream(HttpServletRequest request) {
    try {
      int len = request.getContentLength();
      ServletInputStream originalSIS = request.getInputStream();
      if (len < 0) {
        in = originalSIS;
      } else {
        inputBuffer = new byte[len];
        int n = originalSIS.read(inputBuffer, 0, len);
        assert n == len;
        this.in = new ByteArrayInputStream(inputBuffer);       
        originalSIS.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  byte[] getInputBuffer() {
    return inputBuffer;
  }
}
