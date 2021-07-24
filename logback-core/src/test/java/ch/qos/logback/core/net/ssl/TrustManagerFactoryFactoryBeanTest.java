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

import javax.net.ssl.TrustManagerFactory;

import org.junit.Test;


/**
 * Unit tests for {@link TrustManagerFactoryFactoryBean}.
 *
 * @author Carl Harris
 */
public class TrustManagerFactoryFactoryBeanTest {

    private TrustManagerFactoryFactoryBean factoryBean = new TrustManagerFactoryFactoryBean();

    @Test
    public void testDefaults() throws Exception {
        assertNotNull(factoryBean.createTrustManagerFactory());
    }

    @Test
    public void testExplicitAlgorithm() throws Exception {
        factoryBean.setAlgorithm(TrustManagerFactory.getDefaultAlgorithm());
        assertNotNull(factoryBean.createTrustManagerFactory());
    }

    @Test
    public void testExplicitProvider() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factoryBean.setProvider(factory.getProvider().getName());
        assertNotNull(factoryBean.createTrustManagerFactory());
    }

}
