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
package ch.qos.logback.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import ch.qos.logback.core.status.ErrorStatus;

/**
 * WriterAppender appends events to a hava.io.Writer. This class provides basic
 * services that other appenders build upon.
 * 
 * For more information about this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#WriterAppender
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class WriterAppender<E> extends UnsynchronizedAppenderBase<E> {

  /**
   * Immediate flush means that the underlying writer or output stream will be
   * flushed at the end of each append operation. Immediate flush is slower but
   * ensures that each append request is actually written. If
   * <code>immediateFlush</code> is set to <code>false</code>, then there
   * is a good chance that the last few logs events are not actually written to
   * persistent media if and when the application crashes.
   * 
   * <p>
   * The <code>immediateFlush</code> variable is set to <code>true</code> by
   * default.
   */
  private boolean immediateFlush = true;

  /**
   * The encoding to use when opening an InputStream.
   * <p>
   * The <code>encoding</code> variable is set to <code>null</null> by default 
   * which results in the use of the system's default encoding.
   */
  private String encoding;

  /**
   * This is the {@link Writer Writer} where we will write to.
   */
  private Writer writer;

  /**
   * The default constructor does nothing.
   */
  public WriterAppender() {
  }

  /**
   * If the <b>ImmediateFlush</b> option is set to <code>true</code>, the
   * appender will flush at the end of each write. This is the default behavior.
   * If the option is set to <code>false</code>, then the underlying stream
   * can defer writing to physical medium to a later time.
   * <p>
   * Avoiding the flush operation at the end of each append results in a
   * performance gain of 10 to 20 percent. However, there is safety tradeoff
   * involved in skipping flushing. Indeed, when flushing is skipped, then it is
   * likely that the last few log events will not be recorded on disk when the
   * application exits. This is a high price to pay even for a 20% performance
   * gain.
   */
  public void setImmediateFlush(boolean value) {
    immediateFlush = value;
  }

  /**
   * Returns value of the <b>ImmediateFlush</b> option.
   */
  public boolean getImmediateFlush() {
    return immediateFlush;
  }

  /**
   * Checks that requires parameters are set and if everything is in order,
   * activates this appender.
   */
  public void start() {
    int errors = 0;
    if (this.layout == null) {
      addStatus(new ErrorStatus("No layout set for the appender named \""
          + name + "\".", this));
      errors++;
    }

    if (this.writer == null) {
      addStatus(new ErrorStatus("No writer set for the appender named \""
          + name + "\".", this));
      errors++;
    }
    // only error free appenders should be activated
    if (errors == 0) {
      super.start();
    }
  }

  @Override
  protected void append(E eventObject) {
    if (!isStarted()) {
      return;
    }

    subAppend(eventObject);
  }

  /**
   * Stop this appender instance. The underlying stream or writer is also
   * closed.
   * 
   * <p>
   * Stopped appenders cannot be reused.
   */
  public synchronized void stop() {
    closeWriter();
    super.stop();
  }

  /**
   * Close the underlying {@link java.io.Writer}.
   */
  protected void closeWriter() {
    if (this.writer != null) {
      try {
        // before closing we have to output out layout's footer
        writeFooter();
        this.writer.close();
        this.writer = null;
      } catch (IOException e) {
        addStatus(new ErrorStatus("Could not close writer for WriterAppener.",
            this, e));
      }
    }
  }

  /**
   * Returns an OutputStreamWriter when passed an OutputStream. The encoding
   * used will depend on the value of the <code>encoding</code> property. If
   * the encoding value is specified incorrectly the writer will be opened using
   * the default system encoding (an error message will be printed to the
   * loglog.
   */
  protected OutputStreamWriter createWriter(OutputStream os) {
    OutputStreamWriter retval = null;

    String enc = getEncoding();

    try {
      if (enc != null) {
        retval = new OutputStreamWriter(os, enc);
      } else {
        retval = new OutputStreamWriter(os);
      }
    } catch (IOException e) {
      addStatus(new ErrorStatus("Error initializing output writer.", this, e));
      if (enc != null) {
        addStatus(new ErrorStatus("Unsupported encoding?", this));
      }
    }
    return retval;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String value) {
    encoding = value;
  }

  void writeHeader() {
    if (layout != null && (this.writer != null)) {
      try {
        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, layout.getFileHeader());
        appendIfNotNull(sb, layout.getPresentationHeader());
        if (sb.length() > 0) {
          sb.append(CoreConstants.LINE_SEPARATOR);
          // If at least one of file header or presentation header were not
          // null, then append a line separator.
          // This should be useful in most cases and should not hurt.
          writerWrite(sb.toString(), true);
        }

      } catch (IOException ioe) {
        this.started = false;
        addStatus(new ErrorStatus("Failed to write header for appender named ["
            + name + "].", this, ioe));
      }
    }
  }

  private void appendIfNotNull(StringBuilder sb, String s) {
    if (s != null) {
      sb.append(s);
    }
  }

  void writeFooter() {
    if (layout != null && this.writer != null) {
      try {
        StringBuilder sb = new StringBuilder();
        appendIfNotNull(sb, layout.getPresentationFooter());
        appendIfNotNull(sb, layout.getFileFooter());
        if (sb.length() > 0) {
          writerWrite(sb.toString(), true); // force flush
        }
      } catch (IOException ioe) {
        this.started = false;
        addStatus(new ErrorStatus("Failed to write footer for appender named ["
            + name + "].", this, ioe));
      }
    }
  }

  /**
   * <p>
   * Sets the Writer where the log output will go. The specified Writer must be
   * opened by the user and be writable. The <code>java.io.Writer</code> will
   * be closed when the appender instance is closed.
   * 
   * @param writer
   *          An already opened Writer.
   */
  public synchronized void setWriter(Writer writer) {
    // close any previously opened writer
    closeWriter();

    this.writer = writer;
    writeHeader();
  }

  protected void writerWrite(String s, boolean flush) throws IOException {
    this.writer.write(s);
    if (flush) {
      this.writer.flush();
    }
  }

  /**
   * Actual writing occurs here.
   * <p>
   * Most subclasses of <code>WriterAppender</code> will need to override this
   * method.
   * 
   * @since 0.9.0
   */
  protected void subAppend(E event) {
    if (!isStarted()) {
      return;
    }

    try {
      String output = this.layout.doLayout(event);
      synchronized (this) {
        writerWrite(output, this.immediateFlush);
      }
    } catch (IOException ioe) {
      // as soon as an exception occurs, move to non-started state
      // and add a single ErrorStatus to the SM.
      this.started = false;
      addStatus(new ErrorStatus("IO failure in appender", this, ioe));
    }
  }
}
