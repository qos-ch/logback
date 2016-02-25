/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.net.ssl;

import javax.net.ssl.SSLContext;

/**
 * A configuration for an {@link SSLContext}.
 * <p> 
 *
 * @author Carl Harris
 */
public class SSLConfiguration extends SSLContextFactoryBean {

    private SSLParametersConfiguration parameters;

    /**
     * Gets the SSL parameters configuration.
     * @return parameters configuration; if no parameters object was
     *    configured, a default parameters object is returned
     */
    public SSLParametersConfiguration getParameters() {
        if (parameters == null) {
            parameters = new SSLParametersConfiguration();
        }
        return parameters;
    }

    /**
     * Sets the SSL parameters configuration.
     * @param parameters the parameters configuration to set
     */
    public void setParameters(SSLParametersConfiguration parameters) {
        this.parameters = parameters;
    }

}
