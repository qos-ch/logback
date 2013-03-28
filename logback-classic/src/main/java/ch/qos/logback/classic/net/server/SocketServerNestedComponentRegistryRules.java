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

import ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry;
import ch.qos.logback.core.net.ssl.SSLConfiguration;

/**
 * Nested component registry rules for {@link SocketServer}.
 *
 * @author Carl Harris
 */
public class SocketServerNestedComponentRegistryRules {

  public static void addDefaultNestedComponentRegistryRules(
      DefaultNestedComponentRegistry registry) {
    
    registry.add(SocketServer.class, "threadPool", 
        ThreadPoolFactoryBean.class);
    registry.add(SSLSocketServer.class, "ssl",
        SSLConfiguration.class);
  }

}
