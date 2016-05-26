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

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import ch.qos.logback.core.net.ssl.KeyStoreFactoryBean;

/**
 * A {@link KeyStoreFactoryBean} with test instrumentation. 
 *
 * @author Carl Harris
 */
public class MockKeyStoreFactoryBean extends KeyStoreFactoryBean {

    private boolean keyStoreCreated;

    @Override
    public KeyStore createKeyStore() throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException {
        keyStoreCreated = true;
        return super.createKeyStore();
    }

    public boolean isKeyStoreCreated() {
        return keyStoreCreated;
    }

}
