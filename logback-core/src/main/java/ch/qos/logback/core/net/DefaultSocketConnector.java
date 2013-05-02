/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net;

import ch.qos.logback.core.util.DelayStrategy;
import ch.qos.logback.core.util.FixedDelay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.SocketFactory;

/**
 * Default implementation of {@link SocketConnector}.
 *
 * @author Carl Harris
 * @since 1.0.12
 */
public class DefaultSocketConnector implements SocketConnector {


  private final Lock lock = new ReentrantLock();
  private final Condition connectCondition = lock.newCondition();

  private final InetAddress address;
  private final int port;

  private ExceptionHandler exceptionHandler;
  private SocketFactory socketFactory;
  private DelayStrategy delayStrategy;
  private Socket socket;

  /**
   * Constructs a new connector.
   *
   * @param address      address of remote listener
   * @param port         port of remote listener
   * @param initialDelay delay before initial connection attempt
   * @param retryDelay   delay after failed connection attempt
   */
  public DefaultSocketConnector(InetAddress address, int port,
                                int initialDelay, int retryDelay) {
    this(address, port, new FixedDelay(initialDelay, retryDelay));
  }

  /**
   * Constructs a new connector.
   *
   * @param address       address of remote listener
   * @param port          port of remote listener
   * @param delayStrategy strategy for choosing the delay to impose before
   *                      each connection attempt
   */
  public DefaultSocketConnector(InetAddress address, int port,
                                DelayStrategy delayStrategy) {
    this.address = address;
    this.port = port;
    this.delayStrategy = delayStrategy;
  }

  /**
   * Loops until the desired connection is established.
   */
  public void run() {
    preventReuse();
    inCaseOfMissingFieldsFallbackToDefaults();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        Thread.sleep(delayStrategy.nextDelay());
        Socket newSocket = createSocket();
        if(newSocket != null) {
          socket = newSocket;
          signalConnected();
          // connection established, we are done
          break;
        }
      }
    } catch (InterruptedException ex) {
      // we have been interrupted
    }
    System.out.println("Exiting connector");
  }

  private Socket createSocket() {
    Socket newSocket = null;
    try {
      newSocket = socketFactory.createSocket(address, port);
    } catch (IOException ioex) {
      exceptionHandler.connectionFailed(this, ioex);
    }
    return newSocket;
  }

  private void preventReuse() {
    if (socket != null) {
      throw new IllegalStateException("connector cannot be reused");
    }
  }

  private void inCaseOfMissingFieldsFallbackToDefaults() {
    if (exceptionHandler == null) {
      exceptionHandler = new ConsoleExceptionHandler();
    }
    if (socketFactory == null) {
      socketFactory = SocketFactory.getDefault();
    }
  }

  /**
   * Signals any threads waiting on {@code connectCondition} that the
   * connection has been established.
   */
  private void signalConnected() {
    lock.lock();
    try {
      connectCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public Socket awaitConnection() throws InterruptedException {
    return awaitConnection(Long.MAX_VALUE);
  }

  /**
   * {@inheritDoc}
   */
  public Socket awaitConnection(long delay) throws InterruptedException {
    lock.lock();
    try {
      boolean timeout = false;
      while (socket == null && !timeout) {
        timeout = !connectCondition.await(delay, TimeUnit.MILLISECONDS);
      }
      return socket;
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * {@inheritDoc}
   */
  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  /**
   * A default {@link ExceptionHandler} that writes to {@code System.out}
   */
  private static class ConsoleExceptionHandler implements ExceptionHandler {

    public void connectionFailed(SocketConnector connector, Exception ex) {
      System.out.println(ex);
    }

  }

}
