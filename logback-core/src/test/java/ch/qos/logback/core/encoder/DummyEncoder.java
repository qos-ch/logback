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
package ch.qos.logback.core.encoder;

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.CoreConstants;

public class DummyEncoder<E> extends EncoderBase<E> {

  public static final String DUMMY = "dummy" + CoreConstants.LINE_SEPARATOR;
  String val = DUMMY;
  String fileHeader;
  String fileFooter;
  String encodingName;

  public String getEncodingName() {
    return encodingName;
  }

  public void setEncodingName(String encodingName) {
    this.encodingName = encodingName;
  }

  public DummyEncoder() {
  }

  public DummyEncoder(String val) {
    this.val = val;
  }

  public void doEncode(E event, OutputStream os) throws IOException {
    if (encodingName == null) {
      os.write(val.getBytes());
    } else {
      os.write(val.getBytes(encodingName));
    }
  }

  public void close(OutputStream os) throws IOException {
    if(fileFooter == null) {
      return;
    }
    if (encodingName == null) {
      os.write(fileFooter.getBytes());
    } else {
      os.write(fileFooter.getBytes(encodingName));
    }
  }

  public String getFileHeader() {
    return fileHeader;
  }

  public void setFileHeader(String fileHeader) {
    this.fileHeader = fileHeader;
  }

  public String getFileFooter() {
    return fileFooter;
  }

  public void setFileFooter(String fileFooter) {
    this.fileFooter = fileFooter;
  }

}
