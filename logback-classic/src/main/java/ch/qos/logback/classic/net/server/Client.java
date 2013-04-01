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

import java.io.Closeable;

import ch.qos.logback.classic.LoggerContext;

/**
 * A client of a {@link ServerRunner}.
 * <p>
 * This interface exists primarily to abstract away the details of the
 * client's underlying {@code Socket} and the concurrency associated with
 * handling multiple clients.  Such realities make it difficult to create 
 * effective unit tests for the {@link ServerRunner} that are easy to 
 * understand and maintain.
 * <p>
 * This interface captures the only those details about a client that
 * the {@code ServerRunner} cares about; namely, that it is something that
 * <ol>
 *   <li>has (needs) a {@link LoggerContext}</li>
 *   <li>is Runnable &mdash; i.e. it can be executed concurrently</li>
 *   <li>holds resources that need to be closed before the client is
 *       discarded</li>
 * </ol>
 * 
 * @author Carl Harris
 */
interface Client extends Runnable, Closeable {

  /**
   * Sets the client's logger context.
   * <p>
   * This method <em>must</em> be invoked before the {@link #run()} method.
   * @param lc the logger context to set
   */
  void setLoggerContext(LoggerContext lc);
  
  /**
   * Closes any resources that are held by the client.
   * <p>
   * Note that (as described in Doug Lea's discussion about interrupting I/O
   * operations in "Concurrent Programming in Java" (Addison-Wesley 
   * Professional, 2nd edition, 1999) this method is used to interrupt
   * any blocked I/O operation in the client when the server is shutting
   * down.  The client implementation must anticipate this potential,
   * and gracefully exit when the blocked I/O operation throws the
   * relevant {@link IOException} subclass. 
   * <p>
   * Note also, that unlike {@link Closeable#close()} this method is not
   * permitted to propagate any {@link IOException} that occurs when closing
   * the underlying resource(s).
   */
  void close();
  
}
