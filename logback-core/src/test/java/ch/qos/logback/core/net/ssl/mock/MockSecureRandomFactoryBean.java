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
import java.security.SecureRandom;

import ch.qos.logback.core.net.ssl.SecureRandomFactoryBean;

/**
 * A {@link SecureRandomFactoryBean} with test instrumentation.
 *
 * @author Carl Harris
 */
public class MockSecureRandomFactoryBean extends SecureRandomFactoryBean {

    private boolean secureRandomCreated;

    @Override
    public SecureRandom createSecureRandom() throws NoSuchProviderException, NoSuchAlgorithmException {
        secureRandomCreated = true;
        return super.createSecureRandom();
    }

    public boolean isSecureRandomCreated() {
        return secureRandomCreated;
    }

}
