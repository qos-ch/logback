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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.Layout;

public class LayoutWrappingEncoder<E> extends EncoderBase<E> {

    protected Layout<E> layout;

    /**
     * The charset to use when converting a String into bytes.
     * <p/>
     * By default this property has the value
     * <code>null</null> which corresponds to
     * the system's default charset.
     */
    private Charset charset;

    private boolean immediateFlush = true;

    /**
     * Sets the immediateFlush option. The default value for immediateFlush is 'true'. If set to true,
     * the doEncode() method will immediately flush the underlying OutputStream. Although immediate flushing
     * is safer, it also significantly degrades logging throughput.
     *
     * @since 1.0.3
     */
    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    public boolean isImmediateFlush() {
        return immediateFlush;
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Set the charset to use when converting the string returned by the layout
     * into bytes.
     * <p/>
     * By default this property has the value
     * <code>null</null> which corresponds to
     * the system's default charset.
     *
     * @param charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void init(OutputStream os) throws IOException {
        super.init(os);
        writeHeader();
    }

    void writeHeader() throws IOException {
        if (layout != null && (outputStream != null)) {
            StringBuilder sb = new StringBuilder();
            appendIfNotNull(sb, layout.getFileHeader());
            appendIfNotNull(sb, layout.getPresentationHeader());
            if (sb.length() > 0) {
                sb.append(CoreConstants.LINE_SEPARATOR);
                // If at least one of file header or presentation header were not
                // null, then append a line separator.
                // This should be useful in most cases and should not hurt.
                outputStream.write(convertToBytes(sb.toString()));
                outputStream.flush();
            }
        }
    }

    public void close() throws IOException {
        writeFooter();
    }

    void writeFooter() throws IOException {
        if (layout != null && outputStream != null) {
            StringBuilder sb = new StringBuilder();
            appendIfNotNull(sb, layout.getPresentationFooter());
            appendIfNotNull(sb, layout.getFileFooter());
            if (sb.length() > 0) {
                outputStream.write(convertToBytes(sb.toString()));
                outputStream.flush();
            }
        }
    }

    private byte[] convertToBytes(String s) {
        if (charset == null) {
            return s.getBytes();
        } else {
            try {
                return s.getBytes(charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("An existing charset cannot possibly be unsupported.");
            }
        }
    }

    public void doEncode(E event) throws IOException {
        String txt = layout.doLayout(event);
        outputStream.write(convertToBytes(txt));
        if (immediateFlush)
            outputStream.flush();
    }

    public boolean isStarted() {
        return false;
    }

    public void start() {
        started = true;
    }

    public void stop() {
        started = false;
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {
            }
        }
    }

    private void appendIfNotNull(StringBuilder sb, String s) {
        if (s != null) {
            sb.append(s);
        }
    }

}
