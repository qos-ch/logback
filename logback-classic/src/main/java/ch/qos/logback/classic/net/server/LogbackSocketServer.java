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

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.net.SimpleSocketServer;
import ch.qos.logback.classic.net.ssl.SSLParametersFactoryBean;
import ch.qos.logback.classic.net.ssl.SimpleSSLSocketServer;
import ch.qos.logback.classic.util.LocationUtil;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;

/**
 * A logging socket server that is configurable using Joran.
 *
 * @author Carl Harris
 */
public class LogbackSocketServer {

  public static void main(String[] args) {
    int rc = new LogbackSocketServer().run(args);
    if (rc != 0) {
      System.exit(rc);
    }
  }
  
  /**
   * Runs the socket server using the given arguments.
   * @param args command-line arguments
   * @return process exit code (zero indicates a normal server exit)
   */
  public int run(String[] args) {
    ServerContext context = new ServerContext(); 
    if (args.length != 1) {
      context.addError("usage: " 
            + LogbackSocketServer.class.getCanonicalName() 
            + " config-location");
      return 1;
    }
    
    URL url = getUrlForResource(args[0], context);
    
    SimpleSocketServer server = createServer(url, context);
    if (server != null) {
      server.start();
    }
    
    if (context.hasErrors()) {
      return 1;
    }
    
    return 0;
  }
  
  /**
   * Creates a new socket server configured via a Joran configuration.
   * @param location location of configuration resource
   * @param context Joran context for reporting errors
   * @return socket server or {@code null} if a server socket could not
   *    be created due to an error
   */
  public SimpleSocketServer createServer(URL location, ServerContext context) {
    ServerConfigurator configurator = new ServerConfigurator();
    try {
      configurator.setContext(context);
      configurator.doConfigure(location);
    }
    catch (JoranException ex) {
      context.addError(ex.getMessage(), ex);
    }

    if (context.hasErrors()) {
      return null;
    }
    
    ServerConfiguration config = configurator.getConfiguration();
    
    LoggerContext lc = createLoggerContext(config.getConfiguration(), context);    
    
    if (config.getSsl() != null) {
      return createSSLSocketServer(config, lc, context);      
    }
    
    return createSocketServer(config, lc, context);
  }

  /**
   * Creates an ordinary (non-SSL) socket server.
   * @param config server configuration
   * @param lc logger context for the server
   * @param context Joran context for reporting errors
   * @return socket server or {@code null} if a server socket could not
   *    be created due to an error
   */
  private SimpleSocketServer createSocketServer(ServerConfiguration config,
      LoggerContext lc, ServerContext context) {
    try {
      return new SimpleSocketServer(lc, 
          config.getListener().getPort(), 
          config.getListener().getBacklog(),
          config.getListener().getInetAddress());
    }
    catch (UnknownHostException ex) {
      context.addError(config.getListener().getAddress() + ": unknown host");
      return null;
    }
  }
  
  /**
   * Creates a secure (SSL) socket server.
   * @param config server configuration
   * @param lc logger context for the server
   * @param context Joran context for reporting errors
   * @return socket server or {@code null} if a server socket could not
   *    be created due to an error
   */
  private SimpleSocketServer createSSLSocketServer(
      ServerConfiguration config, LoggerContext lc, ServerContext context) {
    try {
      SSLContext sslContext = config.getSsl().createContext();
      SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
      SSLParametersFactoryBean parameters = config.getSsl().getParameters();
      if (parameters != null) {
        sslParameters = parameters.createSSLParameters(sslParameters);
      }
      return new SimpleSSLSocketServer(lc, 
          config.getListener().getPort(), 
          config.getListener().getBacklog(),
          config.getListener().getInetAddress(),
          sslContext, sslParameters);
    }
    catch (UnknownHostException ex) {
      context.addError(config.getListener().getAddress() + ": unknown host");
      return null;
    }
    catch (Exception ex) {
      context.addError(ex.getMessage());
      return null;
    }
  }
  
  /**
   * Creates the logger context for a socket server.
   * @param location location of the logger's configuration resource
   * @param context Joran context for reporting errors
   * @return logger context or {@code null} if the logger context could
   *    not be created due to an error
   */
  private LoggerContext createLoggerContext(String location, 
      ServerContext context) {
    
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();
    
    URL url = getUrlForResource(location, context);
    if (url == null) return null;

    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      configurator.doConfigure(url);
    }
    catch (Exception ex) {
      StatusUtil.addError(context, this, ex.getMessage(), null);
    }
    
    if (context.hasErrors()) return null;
    
    return lc;
  }

  /**
   * Gets the URL for resource.
   * @param location string representation of the resource location
   * @param context server context for reporting errors
   * @return URL or {@code null} if the URL could not be resolved due
   *    to an error
   */
  private URL getUrlForResource(String location, ServerContext context) {
    URL url = null;
    try {
      url = LocationUtil.urlForResource(location);
    }
    catch (MalformedURLException ex) {
      context.addError(location + ": malformed URL");
    }
    catch (FileNotFoundException ex) {
      context.addError(location + ": resource not found on classpath");
    }
    return url;
  }
  
}
