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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A concurrent {@link ServerRunner}.
 * <p>
 * An instance of this object is created with a {@link ServerListener} and
 * an {@link Executor}.  On invocation of the {@link #start()} method, it
 * passes itself to the given {@code Executor} and returns immediately.  On
 * invocation of its {@link #run()} method by the {@link Executor} it begins 
 * accepting client connections via its {@code ServerListener}.  As each
 * new {@link Client} is accepted, the client is configured with the 
 * runner's {@link LoggingContext} and is then passed to the {@code 
 * Executor} for concurrent execution of the client's service loop.     
 * <p>
 * On invocation of the {@link #stop()} method, the runner closes the listener
 * and each of the connected clients (by invoking {@link Client#close()} 
 * effectively interrupting any blocked I/O calls and causing these concurrent
 * subtasks to exit gracefully).  This ensures that before the {@link #stop()}
 * method returns (1) all I/O resources have been released and (2) all 
 * of the threads of the {@code Executor} are idle.
 *
 * @author Carl Harris
 */
public abstract class ConcurrentServerRunner<T extends Client> 
    extends ContextAwareBase 
    implements Runnable, ServerRunner<T> {

  private final Lock clientsLock = new ReentrantLock();
  
  private final Collection<T> clients = new ArrayList<T>();

  private final ServerListener<T> listener;
  private final Executor executor;
  
  private boolean started;
  
  /**
   * Constructs a new server runner.
   * @param listener the listener from which the server will accept new
   *    clients
   * @param executor a executor that will facilitate execution of the
   *    listening and client-handling tasks; while any {@link Executor}
   *    is allowed here, outside of unit testing the only reasonable choice
   *    is a bounded thread pool of some kind.
   */
  public ConcurrentServerRunner(ServerListener<T> listener, Executor executor) {
    this.listener = listener;
    this.executor = executor;
  }
  
  /**
   * {@inheritDoc}
   */
  public void start() throws IOException {
    if (isStarted()) return;
    executor.execute(this);
    started = true;
  }

  /**
   * {@inheritDoc}
   */
  public void stop() throws IOException {
    if (!isStarted()) return;
    listener.close();
    accept(new ClientVisitor<T>() {
      public void visit(T client) {
        client.close();
      } 
    });
    started = false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * {@inheritDoc}
   */
  public void accept(ClientVisitor<T> visitor) {
    Collection<T> clients = copyClients();
    for (T client : clients) {
      try {
        visitor.visit(client);
      }
      catch (RuntimeException ex) {
        logError(client + ": " + ex);
      }
    }
  }

  private Collection<T> copyClients() {
    clientsLock.lock();
    try {
      Collection<T> copy = new ArrayList<T>(clients);
      return copy;
    }
    finally {
      clientsLock.unlock();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void run() {
    try {
      logInfo("listening on " + listener);
      while (!Thread.currentThread().isInterrupted()) {
        T client = listener.acceptClient();
        if (!configureClient(client)) {
          logError(client + ": connection dropped");
          client.close();
          continue;
        }
        try {
          executor.execute(new ClientWrapper(client));
        }
        catch (RejectedExecutionException ex) {
          logError(client + ": connection dropped");
          client.close();
        }
      }
    }
    catch (InterruptedException ex) {
      assert true;  // ok... we'll shut down
    }
    catch (SocketException ex) {
      logInfo(ex.toString());
    }
    catch (Exception ex) {
      logError(ex.toString());      	
    }
    
    logInfo("shutting down");
    listener.close();
  }

  protected abstract boolean configureClient(T client);
  
  protected abstract void logInfo(String message);
  
  protected abstract void logError(String message);
  
  private void addClient(T client) {
    clientsLock.lock();
    try {
      clients.add(client);
    }
    finally {
      clientsLock.unlock();
    }
  }
  
  private void removeClient(T client) {
    clientsLock.lock();
    try {
      clients.remove(client);
    }
    finally {
      clientsLock.unlock();
    }
  }
  
  private class ClientWrapper implements Client {
    
    private final T delegate;
    
    public ClientWrapper(T client) {
      this.delegate = client;
    }

    public void run() {
      addClient(delegate);
      try {
        delegate.run();
      }
      finally {
        removeClient(delegate);
      }      
    }

    public void close() {
      delegate.close();
    }
    
  }
  
}
