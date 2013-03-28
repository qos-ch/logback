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
package ch.qos.logback.core.net.ssl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.net.ssl.mock.MockContextAware;
import ch.qos.logback.core.net.ssl.mock.MockKeyManagerFactoryFactoryBean;
import ch.qos.logback.core.net.ssl.mock.MockKeyStoreFactoryBean;
import ch.qos.logback.core.net.ssl.mock.MockSecureRandomFactoryBean;
import ch.qos.logback.core.net.ssl.mock.MockTrustManagerFactoryFactoryBean;

/**
 * Unit tests for {@link SSLContextFactoryBean}.
 *
 * @author Carl Harris
 */
public class SSLContextFactoryBeanTest {

  private MockKeyManagerFactoryFactoryBean keyManagerFactory =
      new MockKeyManagerFactoryFactoryBean();
  
  private MockTrustManagerFactoryFactoryBean trustManagerFactory =
      new MockTrustManagerFactoryFactoryBean();
  
  private MockKeyStoreFactoryBean keyStore =
      new MockKeyStoreFactoryBean();
  
  private MockKeyStoreFactoryBean trustStore = 
      new MockKeyStoreFactoryBean();
  
  private MockSecureRandomFactoryBean secureRandom =
      new MockSecureRandomFactoryBean();
  
  private MockContextAware context = new MockContextAware();
  private SSLContextFactoryBean factoryBean = new SSLContextFactoryBean();
  
  @Before
  public void setUp() throws Exception {
    keyStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
    trustStore.setLocation(SSLTestConstants.KEYSTORE_JKS_RESOURCE);
  }

  @Test
  public void testCreateDefaultContext() throws Exception {
    // should be able to create a context with no configuration at all
    assertNotNull(factoryBean.createContext(context));
  }
  
  @Test
  public void testCreateContext() throws Exception {
    factoryBean.setKeyManagerFactory(keyManagerFactory);
    factoryBean.setKeyStore(keyStore);
    factoryBean.setTrustManagerFactory(trustManagerFactory);
    factoryBean.setTrustStore(trustStore);
    factoryBean.setSecureRandom(secureRandom);
    
    assertNotNull(factoryBean.createContext(context));

    assertTrue(keyManagerFactory.isFactoryCreated());
    assertTrue(trustManagerFactory.isFactoryCreated());
    assertTrue(keyStore.isKeyStoreCreated());
    assertTrue(trustStore.isKeyStoreCreated());
    assertTrue(secureRandom.isSecureRandomCreated());
  }
  
}
