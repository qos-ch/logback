/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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

import java.nio.charset.Charset;

import ch.qos.logback.core.CoreConstants;

public class DummyEncoder<E> extends EncoderBase<E> {

    public static final String DUMMY = "dummy" + CoreConstants.LINE_SEPARATOR;
    String val = DUMMY;
    String fileHeader;
    String fileFooter;
    Charset charset;

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public DummyEncoder() {
    }

    public DummyEncoder(String val) {
        this.val = val;
    }

    public byte[] encode(E event)  {
        return encodeString(val);
    }

    byte[] encodeString(String s) {
        if (charset == null) {
            return s.getBytes();
        } else {
            return s.getBytes(charset);
        }
    }

    private void appendIfNotNull(StringBuilder sb, String s) {
        if (s != null) {
            sb.append(s);
        }
    }

    byte[] header() {
        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, fileHeader);
        if (sb.length() > 0) {
            // If at least one of file header or presentation header were not
            // null, then append a line separator.
            // This should be useful in most cases and should not hurt.
            sb.append(CoreConstants.LINE_SEPARATOR);
        }
        return encodeString(sb.toString());
    }

    public byte[] headerBytes() {
        return header();
    }

    public byte[] footerBytes()  {
        if (fileFooter == null) {
            return null;
        }
        return encodeString(fileFooter);
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
