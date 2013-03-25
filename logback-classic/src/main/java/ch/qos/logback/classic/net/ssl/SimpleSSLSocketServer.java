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
import java.security.NoSuchAlgorithmException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SimpleSocketServer;

/**
 * A {@link SimpleSocketServer} that supports SSL.
 * 
 * <pre>
 *      &lt;b&gt;Usage:&lt;/b&gt; java ch.qos.logback.classic.net.ssl.SimpleSSLSocketServer port configFile
 * </pre>
 * 
 * where <em>port</em> is a port number where the server listens and
 * <em>configFile</em> is an xml configuration file fed to
 * {@link JoranConfigurator}.
 * 
 * </pre>
 * 
 * @author Carl Harris
 * 
 * @since 1.0.11
 */
public class SimpleSSLSocketServer extends SimpleSocketServer {

  private final ServerSocketFactory socketFactory;
  
  public SimpleSSLSocketServer(LoggerContext lc, int port) 
      throws NoSuchAlgorithmException {
    this(lc, port, SSLContext.getDefault());
  }

  public SimpleSSLSocketServer(LoggerContext lc, int port,
      SSLContext context) {
    this(lc, port, SimpleSocketServer.DEFAULT_BACKLOG, null, 
        context, context.getDefaultSSLParameters());
  }
  
  public SimpleSSLSocketServer(LoggerContext lc, int port,
      int backlog, InetAddress address, SSLContext context, 
      SSLParameters parameters) {
    super(lc, port, backlog, address);
    if (context == null) {
      throw new NullPointerException("SSL context required");
    }
    if (parameters == null) {
      throw new NullPointerException("SSL parameters required");
    }
    this.socketFactory = new ConfigurableSSLServerSocketFactory(
        parameters, context.getServerSocketFactory());
  }
  
  @Override
  protected ServerSocketFactory getServerSocketFactory() {
    return socketFactory;
  }

}
