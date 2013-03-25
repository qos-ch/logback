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
package ch.qos.logback.classic.net.ssl;

import java.net.InetAddress;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import ch.qos.logback.classic.net.SocketAppender;

/**
 * A {@link SocketAppender} that supports SSL.
 * <p>
 * For more information on this appender, please refer to the online manual
 * at http://logback.qos.ch/manual/appenders.html#SSLSocketAppender
 * 
 * @author Carl Harris
 */
public class SSLSocketAppender extends SocketAppender {

  private SSLConfiguration ssl;

  private SocketFactory socketFactory;
  
  public SSLSocketAppender() {
    super();
  }

  /**
   * Connects to remote server at <code>address</code> and <code>port</code>.
   */
  public SSLSocketAppender(InetAddress address, int port) {
    super(address, port);
  }

  /**
   * Connects to remote server at <code>address</code> and <code>port</code>.
   */
  public SSLSocketAppender(String host, int port) {
    super(host, port);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected SocketFactory getSocketFactory() {
    return socketFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    try {
      SSLContext sslContext = createContext();
      SSLParameters parameters = createParameters(sslContext);
      socketFactory = new ConfigurableSSLSocketFactory(parameters, 
          sslContext.getSocketFactory());
      super.start();
    }
    catch (Exception ex) {
      addError(ex.getMessage(), ex);
    }
  }

  /**
   * Creates an {@link SSLContext} according to the receiver's
   * SSL context configuration or a default context if no configuration
   * has been specified.
   * @return SSL context object
   * @throws Exception if any error occurs in creating the context
   */
  private SSLContext createContext() throws Exception {
    if (getSsl() != null) {
      return getSsl().createContext(this);
    }
    return SSLContext.getDefault();
  }

  /**
   * Creates an {@link SSLParameters} according to the receiver's 
   * SSL parameters configuration or the context's default parameters if
   * no configuration has been specified.
   * @param sslContext SSL context
   * @return parameters object
   */
  private SSLParameters createParameters(SSLContext sslContext) {
    if (getSsl().getParameters() != null) {
      return getSsl().getParameters().createSSLParameters(
        sslContext.getDefaultSSLParameters(), this);
    }
    return sslContext.getDefaultSSLParameters();
  }

  /**
   * Gets the SSL configuration.
   * @return SSL configuration
   */
  public SSLConfiguration getSsl() {
    return ssl;
  }

  /**
   * Sets the SSL configuration.
   * @param ssl the SSL configuration to set
   */
  public void setSsl(SSLConfiguration ssl) {
    this.ssl = ssl;
  }
      
}
