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
package ch.qos.logback.core.net.ssl.mock;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.net.ssl.TrustManagerFactory;

import ch.qos.logback.core.net.ssl.TrustManagerFactoryFactoryBean;

/**
 * A {@link TrustManagerFactoryFactoryBean} with test instrumentation.
 *
 * @author Carl Harris
 */
public class MockTrustManagerFactoryFactoryBean extends TrustManagerFactoryFactoryBean {

    private boolean factoryCreated;

    @Override
    public TrustManagerFactory createTrustManagerFactory() throws NoSuchProviderException, NoSuchAlgorithmException {
        factoryCreated = true;
        return super.createTrustManagerFactory();
    }

    public boolean isFactoryCreated() {
        return factoryCreated;
    }

}
