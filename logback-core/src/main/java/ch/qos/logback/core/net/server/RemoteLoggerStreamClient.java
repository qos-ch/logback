/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

package ch.qos.logback.core.net.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A {@link RemoteLoggerClient} that writes serialized logging events to an
 * {@link OutputStream}.
 *
 * @author Carl Harris
 */
public class RemoteLoggerStreamClient 
    extends ContextAwareBase implements RemoteLoggerClient {

  private final String id;
  private final Socket socket;
  
  private int queueSize;
  private volatile BlockingQueue<Serializable> queue;
  
  /**
   * Constructs a new client.
   * @param id identifier string for the client
   * @param socket socket to which logging events will be written
   */
  public RemoteLoggerStreamClient(String id, Socket socket) {
    this.id = id;
    this.socket = socket;
  }

  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  /**
   * {@inheritDoc}
   */
  public boolean offer(Serializable event) {
    if (queue == null) {
      throw new IllegalStateException("client is not running");
    }
    return queue.offer(event);
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    try {
      socket.close();
    }
    catch (IOException ex) {
      ex.printStackTrace(System.err);
    }    
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    if (getContext() == null) {
      throw new IllegalStateException("context is not configured");
    }
    queue = new ArrayBlockingQueue<Serializable>(queueSize);
    try {
      int counter = 0;
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Serializable event = queue.take();
          oos.writeObject(event);
          oos.flush();
          if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
            // failing to reset the stream periodically will result in a
            // serious memory leak (as noted in SocketAppenderBase)
            counter = 0;
            oos.reset();
          }
        }
        catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    }
    catch (SocketException ex) {
      addInfo(this + ": " + ex);
    }
    catch (IOException ex) {
      addError(this + ": " + ex);
    }
    catch (RuntimeException ex) {
      addError(this + ": " + ex);
    }
    finally {
      addInfo(this + ": connection closed");
      close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "client " + id;
  }
 
}
