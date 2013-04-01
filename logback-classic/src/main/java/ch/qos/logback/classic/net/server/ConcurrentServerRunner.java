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
package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
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
class ConcurrentServerRunner extends ContextAwareBase implements Runnable, ServerRunner {

  private final Lock clientsLock = new ReentrantLock();
  
  private final Collection<Client> clients = new ArrayList<Client>();

  private final ServerListener listener;
  private final Executor executor;
  
  private LoggerContext lc;
  private Logger logger;
  private boolean started;
  
  /**
   * Constructs a new server runner.
   * @param listener the listener from which the server will accept new
   *    clients
   * @param executor a executor that will facilitate execution of the
   *    listening and client-handling tasks; while any {@link Executor}
   *    is allowed here, outside of unit testing the only reasonable choice
   *    is a bounded thread pool of some kind.  If the executor passed here 
   *    is a {@link ThreadPoolExecutor} the runner configures a rejected 
   *    execution handler to ensure that when a client cannot be accomodated
   *    it is summarily closed to prevent resource leaks.
   */
  public ConcurrentServerRunner(ServerListener listener, Executor executor) {
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
    Collection<Client> clients = new ArrayList<Client>(this.clients);
    for (Client client : clients) {
      client.close();
    }
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
  public void run() {
    try {
      logInfo("listening on " + listener);
      while (!Thread.currentThread().isInterrupted()) {
        Client client = listener.acceptClient();
        LoggerContext lc = getLoggerContext();
        if (lc == null) {
          logError("logger context not yet configured -- "
              + "dropping client connection " + client); 
          client.close();
          continue;
        }
        
        client.setLoggerContext(lc);
        executor.execute(new ClientWrapper(client));
      }
    }
    catch (InterruptedException ex) {
      // ok... we'll shut down
    }
    catch (Exception ex) {
      logError(ex.toString());      	
    }
    
    logInfo("shutting down");
    listener.close();
  }
  
  private void logInfo(String message) {
    Logger logger = getLogger();
    if (logger != null) {
      logger.info(message);
    }
    else {
      addInfo(message);
    }
  }
  
  private void logError(String message) {
    Logger logger = getLogger();
    if (logger != null) {
      logger.error(message);
    }
    else {
      addError(message);
    }
  }
  
  private Logger getLogger() {
    if (logger == null) {
      LoggerContext lc = getLoggerContext();
      if (lc != null) {
        logger = lc.getLogger(getClass().getPackage().getName());
      }
    }
    return logger;
  }
  
  private LoggerContext getLoggerContext() {
    if (lc == null) {   
      ILoggerFactory factory = LoggerFactory.getILoggerFactory();
      if (factory instanceof LoggerContext) {
        lc = (LoggerContext) factory;
      }
    }
    return lc;
  }
  
  private void addClient(Client client) {
    clientsLock.lock();
    try {
      clients.add(client);
    }
    finally {
      clientsLock.unlock();
    }
  }
  
  private void removeClient(Client client) {
    clientsLock.lock();
    try {
      clients.remove(client);
    }
    finally {
      clientsLock.unlock();
    }
  }
  
  private class ClientWrapper implements Client {
    
    private final Client delegate;
    
    public ClientWrapper(Client client) {
      this.delegate = client;
    }

    public void setLoggerContext(LoggerContext lc) {
      delegate.setLoggerContext(lc);
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
