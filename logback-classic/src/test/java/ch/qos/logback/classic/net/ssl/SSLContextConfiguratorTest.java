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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.SSLContext;

import org.junit.Test;

import ch.qos.logback.classic.net.ssl.mock.MockKeyManagerFactoryConfigurator;
import ch.qos.logback.classic.net.ssl.mock.MockKeyStoreConfigurator;
import ch.qos.logback.classic.net.ssl.mock.MockSecureRandomConfigurator;
import ch.qos.logback.classic.net.ssl.mock.MockTrustManagerFactoryConfigurator;

/**
 * Unit tests for {@link SSLContextFactoryBean}.
 *
 * @author Carl Harris
 */
public class SSLContextConfiguratorTest {

  private SSLContextFactoryBean configurator = new SSLContextFactoryBean();
  
  @Test
  public void testDefaults() throws Exception {
    assertNotNull(configurator.createContext());
  }
  
  @Test
  public void testExplicitProtocol() throws Exception {
    configurator.setProtocol(SSL.DEFAULT_PROTOCOL);
    assertNotNull(configurator.createContext());    
  }

  @Test
  public void testExplicitProvider() throws Exception {
    configurator.setProvider(SSLContext.getDefault().getProvider().getName());
    assertNotNull(configurator.createContext());    
  }
  
  @Test
  public void testExplicitKeyStore() throws Exception {
    MockKeyStoreConfigurator keyStore = new MockKeyStoreConfigurator();
    keyStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
    configurator.setKeyStore(keyStore);
    assertNotNull(configurator.createContext());
    assertTrue(keyStore.isKeyStoreCreated());
  }
  
  @Test
  public void testExplicitTrustStore() throws Exception {
    MockKeyStoreConfigurator trustStore = new MockKeyStoreConfigurator();
    trustStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
    configurator.setTrustStore(trustStore);
    assertNotNull(configurator.createContext());
    assertTrue(trustStore.isKeyStoreCreated());
  }
  
  @Test
  public void testExplicitSecureRandom() throws Exception {
    MockSecureRandomConfigurator secureRandom = 
        new MockSecureRandomConfigurator();
    configurator.setSecureRandom(secureRandom);
    assertNotNull(configurator.createContext());
    assertTrue(secureRandom.isSecureRandomCreated());
  }
  
  @Test
  public void testExplicitKeyManagerFactory() throws Exception {
    MockKeyStoreConfigurator keyStore = new MockKeyStoreConfigurator();
    keyStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);

    MockKeyManagerFactoryConfigurator keyManagerFactory = 
        new MockKeyManagerFactoryConfigurator();

    configurator.setKeyStore(keyStore);
    configurator.setKeyManagerFactory(keyManagerFactory);
    assertNotNull(configurator.createContext());
    assertTrue(keyManagerFactory.isFactoryCreated());
  }

  @Test
  public void testExplicitTrustManagerFactory() throws Exception {
    MockKeyStoreConfigurator trustStore = new MockKeyStoreConfigurator();
    trustStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);

    MockTrustManagerFactoryConfigurator trustManagerFactory =
        new MockTrustManagerFactoryConfigurator();
    
    configurator.setTrustStore(trustStore);
    configurator.setTrustManagerFactory(trustManagerFactory);
    assertNotNull(configurator.createContext());    
    assertTrue(trustManagerFactory.isFactoryCreated());
  }

}
