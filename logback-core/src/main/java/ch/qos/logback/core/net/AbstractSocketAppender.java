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
// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;


import javax.net.SocketFactory;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.util.CloseUtil;
import ch.qos.logback.core.util.Duration;

/**
 * An abstract base for module specific {@code SocketAppender}
 * implementations in other logback modules.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * @author Carl Harris
 */

public abstract class AbstractSocketAppender<E> extends AppenderBase<E>
    implements Runnable, SocketConnector.ExceptionHandler {

  /**
   * The default port number of remote logging server (4560).
   */
  public static final int DEFAULT_PORT = 4560;

  /**
   * The default reconnection delay (30000 milliseconds or 30 seconds).
   */
  public static final int DEFAULT_RECONNECTION_DELAY = 30000;

  /**
   * Default size of the queue used to hold logging events that are destined
   * for the remote peer.
   */
  public static final int DEFAULT_QUEUE_SIZE = 128;
  
  /**
   * Default timeout when waiting for the remote server to accept our
   * connection.
   */
  private static final int DEFAULT_ACCEPT_CONNECTION_DELAY = 5000;

  /**
   * Default timeout for how long to wait when inserting an event into
   * the BlockingQueue.
   */
  private static final int DEFAULT_EVENT_DELAY_TIMEOUT = 100;

  private String remoteHost;
  private int port = DEFAULT_PORT;
  private InetAddress address;
  private Duration reconnectionDelay = new Duration(DEFAULT_RECONNECTION_DELAY);
  private int queueSize = DEFAULT_QUEUE_SIZE;
  private int acceptConnectionTimeout = DEFAULT_ACCEPT_CONNECTION_DELAY;
  private Duration eventDelayLimit = new Duration(DEFAULT_EVENT_DELAY_TIMEOUT);
  
  private BlockingQueue<E> queue;
  private String peerId;
  private Future<?> task;
  private Future<Socket> connectorTask;

  private volatile Socket socket;

  /**
   * Constructs a new appender.
   */
  protected AbstractSocketAppender() {
  }
  
  /**
   * Constructs a new appender that will connect to the given remote host 
   * and port.
   * <p>
   * This constructor was introduced primarily to allow the encapsulation 
   * of the this class to be improved in a manner that is least disruptive 
   * to <em>existing</em> subclasses.  <strong>This constructor will be 
   * removed in future release</strong>.
   * 
   * @param remoteHost target remote host
   * @param port target port on remote host
   */
  @Deprecated
  protected AbstractSocketAppender(String remoteHost, int port) {
    this.remoteHost = remoteHost;
    this.port = port;
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() {
    if (isStarted()) return;
    int errorCount = 0;
    if (port <= 0) {
      errorCount++;
      addError("No port was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
    }

    if (remoteHost == null) {
      errorCount++;
      addError("No remote host was configured for appender"
          + name
          + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
    }
    
    if (queueSize < 0) {
      errorCount++;
      addError("Queue size must be non-negative");
    }

    if (errorCount == 0) {
      try {
        address = InetAddress.getByName(remoteHost);
      } catch (UnknownHostException ex) {
        addError("unknown host: " + remoteHost);
        errorCount++;
      }
    }

    if (errorCount == 0) {
      queue = newBlockingQueue(queueSize);
      peerId = "remote peer " + remoteHost + ":" + port + ": ";
      task = getContext().getExecutorService().submit(this);
      super.start();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    if (!isStarted()) return;
    CloseUtil.closeQuietly(socket);
    task.cancel(true);
    if(connectorTask != null)
      connectorTask.cancel(true);
    super.stop();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void append(E event) {
    if (event == null || !isStarted()) return;

    try {
      final boolean inserted = queue.offer(event, eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
      if (!inserted) {
        addInfo("Dropping event due to timeout limit of [" + eventDelayLimit +
            "] milliseconds being exceeded");
      }
    } catch (InterruptedException e) {
      addError("Interrupted while appending event to SocketAppender", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void run() {
    signalEntryInRunMethod();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        SocketConnector connector = createConnector(address, port, 0,
                reconnectionDelay.getMilliseconds());

        connectorTask = activateConnector(connector);
        if(connectorTask == null)
          break;

        socket = waitForConnectorToReturnASocket();
        if(socket == null)
          break;
        dispatchEvents();
      }
    } catch (InterruptedException ex) {
      assert true;    // ok... we'll exit now
    }
    addInfo("shutting down");
  }

    protected void signalEntryInRunMethod() {
      // do nothing by default
    }

    private SocketConnector createConnector(InetAddress address, int port,
                                          int initialDelay, long retryDelay) {
    SocketConnector connector = newConnector(address, port, initialDelay,
            retryDelay);
    connector.setExceptionHandler(this);
    connector.setSocketFactory(getSocketFactory());
    return connector;
  }

  private Future<Socket> activateConnector(SocketConnector connector) {
    try {
      return getContext().getExecutorService().submit(connector);
    } catch (RejectedExecutionException ex) {
      return null;
    }
  }

  private Socket waitForConnectorToReturnASocket() throws InterruptedException {
    try {
      Socket s =  connectorTask.get();
      connectorTask = null;
      return s;
    } catch (ExecutionException e) {
      return null;
    }
  }

  private void dispatchEvents() throws InterruptedException {
    try {
      socket.setSoTimeout(acceptConnectionTimeout);
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      socket.setSoTimeout(0);
      addInfo(peerId + "connection established");
      int counter = 0;
      while (true) {
        E event = queue.take();
        postProcessEvent(event);
        Serializable serEvent = getPST().transform(event);
        oos.writeObject(serEvent);
        oos.flush();
        if (++counter >= CoreConstants.OOS_RESET_FREQUENCY) {
          // Failing to reset the object output stream every now and
          // then creates a serious memory leak.
          oos.reset();
          counter = 0;
        }
      }
    } catch (IOException ex) {
      addInfo(peerId + "connection failed: " + ex);
    } finally {
      CloseUtil.closeQuietly(socket);
      socket = null;
      addInfo(peerId + "connection closed");
    }
  } 
    
  /**
   * {@inheritDoc}
   */
  public void connectionFailed(SocketConnector connector, Exception ex) {
    if (ex instanceof InterruptedException) {
      addInfo("connector interrupted");
    } else if (ex instanceof ConnectException) {
      addInfo(peerId + "connection refused");
    } else {
      addInfo(peerId + ex);
    }
  }



  /**
   * Creates a new {@link SocketConnector}.
   * <p>
   * The default implementation creates an instance of {@link DefaultSocketConnector}.
   * A subclass may override to provide a different {@link SocketConnector}
   * implementation.
   * 
   * @param address target remote address
   * @param port target remote port
   * @param initialDelay delay before the first connection attempt
   * @param retryDelay delay before a reconnection attempt
   * @return socket connector
   */
  protected SocketConnector newConnector(InetAddress address,
                                         int port, long initialDelay, long retryDelay) {
    return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
  }

  /**
   * Gets the default {@link SocketFactory} for the platform.
   * <p>
   * Subclasses may override to provide a custom socket factory.
   */
  protected SocketFactory getSocketFactory() {
    return SocketFactory.getDefault();
  }

  /**
   * Creates a blocking queue that will be used to hold logging events until
   * they can be delivered to the remote receiver.
   * <p>
   * The default implementation creates a (bounded) {@link ArrayBlockingQueue} 
   * for positive queue sizes.  Otherwise it creates a {@link SynchronousQueue}.
   * <p>
   * This method is exposed primarily to support instrumentation for unit
   * testing.
   * 
   * @param queueSize size of the queue
   * @return
   */
  BlockingQueue<E> newBlockingQueue(int queueSize) {
    return queueSize <= 0 ? 
        new SynchronousQueue<E>() : new ArrayBlockingQueue<E>(queueSize);
  }
  
  /**
   * Post-processes an event before it is serialized for delivery to the
   * remote receiver.
   * @param event the event to post-process
   */
  protected abstract void postProcessEvent(E event);
  
  /**
   * Get the pre-serialization transformer that will be used to transform
   * each event into a Serializable object before delivery to the remote
   * receiver.
   * @return transformer object
   */
  protected abstract PreSerializationTransformer<E> getPST();

  /*
   * This method is used by logback modules only in the now deprecated
   * convenience constructors for SocketAppender
   */
  @Deprecated
  protected static InetAddress getAddressByName(String host) {
    try {
      return InetAddress.getByName(host);
    } catch (Exception e) {
      // addError("Could not find address of [" + host + "].", e);
      return null;
    }
  }

  /**
   * The <b>RemoteHost</b> property takes the name of of the host where a corresponding server is running.
   */
  public void setRemoteHost(String host) {
    remoteHost = host;
  }

  /**
   * Returns value of the <b>RemoteHost</b> property.
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * The <b>Port</b> property takes a positive integer representing the port
   * where the server is waiting for connections.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Returns value of the <b>Port</b> property.
   */
  public int getPort() {
    return port;
  }

  /**
   * The <b>reconnectionDelay</b> property takes a positive {@link Duration} value
   * representing the time to wait between each failed connection attempt
   * to the server. The default value of this option is to 30 seconds.
   *
   * <p>
   * Setting this option to zero turns off reconnection capability.
   */
  public void setReconnectionDelay(Duration delay) {
    this.reconnectionDelay = delay;
  }

  /**
   * Returns value of the <b>reconnectionDelay</b> property.
   */
  public Duration getReconnectionDelay() {
    return reconnectionDelay;
  }

  /**
   * The <b>queueSize</b> property takes a non-negative integer representing
   * the number of logging events to retain for delivery to the remote receiver.
   * When the queue size is zero, event delivery to the remote receiver is
   * synchronous.  When the queue size is greater than zero, the 
   * {@link #append(Object)} method returns immediately after enqueing the
   * event, assuming that there is space available in the queue.  Using a 
   * non-zero queue length can improve performance by eliminating delays
   * caused by transient network delays.
   * 
   * @param queueSize the queue size to set.
   */
  public void setQueueSize(int queueSize) {
    this.queueSize = queueSize;
  }

  /**
   * Returns the value of the <b>queueSize</b> property.
   */
  public int getQueueSize() {
    return queueSize;
  }

  /**
   * The <b>eventDelayLimit</b> takes a non-negative integer representing the
   * number of milliseconds to allow the appender to block if the underlying
   * BlockingQueue is full. Once this limit is reached, the event is dropped.
   *
   * @param eventDelayLimit the event delay limit
   */
  public void setEventDelayLimit(Duration eventDelayLimit) {
    this.eventDelayLimit = eventDelayLimit;
  }

  /**
   * Returns the value of the <b>eventDelayLimit</b> property.
   */
  public Duration getEventDelayLimit() {
    return eventDelayLimit;
  }
  
  /**
   * Sets the timeout that controls how long we'll wait for the remote
   * peer to accept our connection attempt.
   * <p>
   * This property is configurable primarily to support instrumentation
   * for unit testing.
   * 
   * @param acceptConnectionTimeout timeout value in milliseconds
   */
  void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
    this.acceptConnectionTimeout = acceptConnectionTimeout;
  }

}
