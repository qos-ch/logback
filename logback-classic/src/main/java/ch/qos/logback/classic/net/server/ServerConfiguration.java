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

/**
 * A configuration for a {@link LogbackSocketServer}.
 *
 * @author Carl Harris
 */
public class ServerConfiguration {

  private String configuration;
  private ListenerConfiguration listener;
  private SSLConfiguration ssl;

  /**
   * Gets the location of the server's logger configuration resource.
   * @return location string
   */
  public String getConfiguration() {
    return configuration;
  }

  /**
   * Sets the location of the server's logger configuration resource.
   * @param configuration the location to set
   */
  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  /**
   * Gets the server's listener configuration
   * @return listener configuration
   */
  public ListenerConfiguration getListener() {
    if (listener == null) {
      return new ListenerConfiguration();
    }
    return listener;
  }

  /**
   * Sets the server's listener configuration.
   * @param listener the listener configuration to set
   */
  public void setListener(ListenerConfiguration listener) {
    this.listener = listener;
  }

  /**
   * Sets the server's SSL configuration.
   * @return SSL configuration or {@code null} if no SSL configuration was
   *    provided
   */
  public SSLConfiguration getSsl() {
    return ssl;
  }

  /**
   * Gets the server's SSL configuration.
   * @param ssl the SSL configuration to set.
   */
  public void setSsl(SSLConfiguration ssl) {
    this.ssl = ssl;
  }

}
