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

import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
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
   * It is the encoder which is ultimately responsible for writing the event to
   * an {@link OutputStream}.
   */
  protected Encoder<E> encoder;

  /**
   * All synchronization in this class is done via the lock object.
   */
  protected Object lock = new Object();

  /**
   * Immediate flush means that the underlying writer or output stream will be
   * flushed at the end of each append operation. Immediate flush is slower but
   * ensures that each append request is actually written. If
   * <code>immediateFlush</code> is set to <code>false</code>, then there is a
   * good chance that the last few logs events are not actually written to
   * persistent media if and when the application crashes.
   * 
   * <p>
   * The <code>immediateFlush</code> variable is set to <code>true</code> by
   * default.
   */
  private boolean immediateFlush = true;

  /**
   * This is the {@link OutputStream outputStream} where output will be written.
   */
  private OutputStream outputStream;

  /**
   * The default constructor does nothing.
   */
  public WriterAppender() {
  }

  /**
   * If the <b>ImmediateFlush</b> option is set to <code>true</code>, the
   * appender will flush at the end of each write. This is the default behavior.
   * If the option is set to <code>false</code>, then the underlying stream can
   * defer writing to physical medium to a later time.
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
   * The underlying output stream used by this appender.
   * 
   * @return
   */
  public OutputStream getOutputStream() {
    return outputStream;
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
    if (this.encoder == null) {
      addStatus(new ErrorStatus("No encoder set for the appender named \""
          + name + "\".", this));
      errors++;
    }

    if (this.outputStream == null) {
      addStatus(new ErrorStatus(
          "No output stream set for the appender named \"" + name + "\".", this));
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
  public void stop() {
    synchronized (lock) {
      closeOutputStream();
      super.stop();
    }
  }

  /**
   * Close the underlying {@link OutputStream}.
   */
  protected void closeOutputStream() {
    if (this.outputStream != null) {
      try {
        // before closing we have to output out layout's footer
        encoderClose();
        this.outputStream.close();
        this.outputStream = null;
      } catch (IOException e) {
        addStatus(new ErrorStatus(
            "Could not close output stream for WriterAppener.", this, e));
      }
    }
  }

  void encoderInit() {
    if (encoder != null && this.outputStream != null) {
      try {
        encoder.init(outputStream);
      } catch (IOException ioe) {
        this.started = false;
        addStatus(new ErrorStatus("Failed to write footer for appender named ["
            + name + "].", this, ioe));
      }
    }
  }

  void encoderClose() {
    if (encoder != null && this.outputStream != null) {
      try {
        encoder.close();
      } catch (IOException ioe) {
        this.started = false;
        addStatus(new ErrorStatus("Failed to write footer for appender named ["
            + name + "].", this, ioe));
      }
    }
  }

  /**
   * <p>
   * Sets the @link OutputStream} where the log output will go. The specified
   * <code>OutputStream</code> must be opened by the user and be writable. The
   * <code>OutputStream</code> will be closed when the appender instance is
   * closed.
   * 
   * @param outputStream
   *          An already opened OutputStream.
   */
  public void setOutputStream(OutputStream outputStream) {
    synchronized (lock) {
      // close any previously opened output stream
      closeOutputStream();

      this.outputStream = outputStream;
      if (encoder == null) {
        addWarn("Encoder not yet set. Cannot invoke it's init method");
        return;
      }

      encoderInit();
    }
  }

  protected void writeOut(E event) throws IOException {
    this.encoder.doEncode(event);
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
      // this step avoids LBCLASSIC-139
      if (event instanceof DeferredProcessingAware) {
        ((DeferredProcessingAware) event).prepareForDeferredProcessing();
      }
      // the synchronized prevents the OutputStream from being closed while we
      // are writing
      synchronized (lock) {
        writeOut(event);
      }
    } catch (IOException ioe) {
      // as soon as an exception occurs, move to non-started state
      // and add a single ErrorStatus to the SM.
      this.started = false;
      addStatus(new ErrorStatus("IO failure in appender", this, ioe));
    }
  }

  public Encoder<E> getEncoder() {
    return encoder;
  }

  public void setEncoder(Encoder<E> encoder) {
    this.encoder = encoder;
  }

}
