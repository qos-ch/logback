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

import static org.junit.Assert.assertNotNull;

import java.security.KeyStore;

import org.junit.Test;

/**
 * Unit tests for {@link KeyStoreFactoryBean}.
 *
 * @author Carl Harris
 */
public class KeyStoreFactoryBeanTest {

    private KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();

    @Test
    public void testDefaults() throws Exception {
        factoryBean.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
        assertNotNull(factoryBean.createKeyStore());
    }

    @Test
    public void testExplicitProvider() throws Exception {
        factoryBean.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
        KeyStore keyStore = factoryBean.createKeyStore();
        factoryBean.setProvider(keyStore.getProvider().getName());
        assertNotNull(factoryBean.createKeyStore());
    }

    @Test
    public void testExplicitType() throws Exception {
        factoryBean.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
        factoryBean.setType(SSL.DEFAULT_KEYSTORE_TYPE);
        assertNotNull(factoryBean.createKeyStore());
    }

    @Test
    public void testPKCS12Type() throws Exception {
        factoryBean.setLocation(SSLTestConstants.KEYSTORE_PKCS12_RESOURCE);
        factoryBean.setType(SSLTestConstants.PKCS12_TYPE);
        assertNotNull(factoryBean.createKeyStore());
    }

    @Test
    public void testExplicitPassphrase() throws Exception {
        factoryBean.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
        factoryBean.setPassword(SSL.DEFAULT_KEYSTORE_PASSWORD);
        assertNotNull(factoryBean.createKeyStore());
    }

}
