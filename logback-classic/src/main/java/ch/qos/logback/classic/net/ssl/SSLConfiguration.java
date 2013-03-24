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

import javax.net.ssl.SSLContext;

/**
 * A configuration for an {@link SSLContext}.
 * <p> 
 *
 * @author Carl Harris
 */
public class SSLConfiguration extends SSLContextFactoryBean {

  private SSLParametersFactoryBean parameters;

  /**
   * Gets the SSL parameters configuration.
   * @return parameters configuration
   */
  public SSLParametersFactoryBean getParameters() {
    return parameters;
  }

  /**
   * Sets the SSL parameters configuration.
   * @param parameters the parameters configuration to set
   */
  public void setParameters(SSLParametersFactoryBean parameters) {
    this.parameters = parameters;
  }

}
