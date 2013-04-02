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

import javax.net.ServerSocketFactory;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * 
 * This is the base class for module specific ServerSocketAppender 
 * implementations.
 * 
 * @author Carl Harris
 */
public abstract class ServerSocketAppenderBase<E> extends AppenderBase<E> {

  /**
   * Gets the factory used to create {@link ServerSocket} objects.
   * <p>
   * The default implementation delegates to 
   * {@link ServerSocketFactory#getDefault()}.  Subclasses may override to
   * private a different socket factory implementation.
   * 
   * @return socket factory.
   */
  protected ServerSocketFactory getServerSocketFactory() {
    return ServerSocketFactory.getDefault();
  }
  
  /**
   * Post process an event received via {@link #append(E)}.
   * @param event
   */
  protected abstract void postProcessEvent(E event);

  /**
   * Gets a transformer that will be used to convert a received event
   * to a {@link Serializable} form.
   * @return
   */
  protected abstract PreSerializationTransformer<E> getPST();


  @Override
  public void start() {

  }

  @Override
  public void stop() {
  
  }

  @Override
  protected void append(E eventObject) {

  }

}

