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

    public void doEncode(E event) throws IOException {
        writeOut(val);
    }

    private void appendIfNotNull(StringBuilder sb, String s) {
        if (s != null) {
            sb.append(s);
        }
    }

    void writeOut(String s) throws IOException {
        if (encodingName == null) {
            outputStream.write(s.getBytes());
        } else {
            outputStream.write(s.getBytes(encodingName));
        }
    }

    void writeHeader() throws IOException {
        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, fileHeader);
        if (sb.length() > 0) {
            sb.append(CoreConstants.LINE_SEPARATOR);
            // If at least one of file header or presentation header were not
            // null, then append a line separator.
            // This should be useful in most cases and should not hurt.
            writeOut(sb.toString());
        }
    }

    public void init(OutputStream os) throws IOException {
        super.init(os);
        writeHeader();
    }

    public void close() throws IOException {
        if (fileFooter == null) {
            return;
        }
        if (encodingName == null) {
            outputStream.write(fileFooter.getBytes());
        } else {
            outputStream.write(fileFooter.getBytes(encodingName));
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
