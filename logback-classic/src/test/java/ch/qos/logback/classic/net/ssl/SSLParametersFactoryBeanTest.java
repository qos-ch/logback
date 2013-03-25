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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.net.ssl.SSLParameters;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.ContextAwareBase;


/**
 * Unit tests for {@link SSLParametersFactoryBean}.
 *
 * @author Carl Harris
 */
public class SSLParametersFactoryBeanTest {

  private final SSLParameters defaults = new SSLParameters(new String[0], new String[0]);
  
  private ContextAwareBase context = new ContextAwareBase();
  
  private SSLParametersFactoryBean factoryBean = 
      new SSLParametersFactoryBean();
 
  @Before
  public void setUp() throws Exception {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();
    context.setContext(lc);
  }
  
  @Test
  public void testSetIncludedProtocols() throws Exception {
    defaults.setProtocols(new String[] { "A", "B", "C", "D" });
    factoryBean.setIncludedProtocols("A,B ,C, D");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(
        new String[] { "A", "B", "C", "D" }, params.getProtocols()));
  }

  @Test
  public void testSetExcludedProtocols() throws Exception {
    defaults.setProtocols(new String[] { "A" });
    factoryBean.setExcludedProtocols("A");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(new String[0], params.getProtocols()));
  }

  @Test
  public void testSetIncludedAndExcludedProtocols() throws Exception {
    defaults.setProtocols(new String[] { "A", "B", "C" });
    factoryBean.setIncludedProtocols("A, B");
    factoryBean.setExcludedProtocols("B");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(new String[] { "A" }, params.getProtocols()));
  }

  @Test
  public void testSetIncludedCipherSuites() throws Exception {
    defaults.setCipherSuites(new String[] { "A", "B", "C", "D" });
    factoryBean.setIncludedCipherSuites("A,B ,C, D");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(
        new String[] { "A", "B", "C", "D" }, params.getCipherSuites()));
  }

  @Test
  public void testSetExcludedCipherSuites() throws Exception {
    defaults.setCipherSuites(new String[] { "A" });
    factoryBean.setExcludedCipherSuites("A");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(new String[0], params.getCipherSuites()));
  }

  @Test
  public void testSetExcludedAndIncludedCipherSuites() throws Exception {
    defaults.setCipherSuites(new String[] { "A", "B", "C" });
    factoryBean.setIncludedCipherSuites("A, B");
    factoryBean.setExcludedCipherSuites("B");
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(new String[] { "A" }, params.getCipherSuites()));
  }

  @Test
  public void testSetNeedClientAuth() throws Exception {
    factoryBean.setNeedClientAuth(true);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(params.getNeedClientAuth());
  }

  @Test
  public void testSetWantClientAuth() throws Exception {
    factoryBean.setWantClientAuth(true);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(params.getWantClientAuth());
  }

  @Test
  public void testPassDefaultProtocols() throws Exception {
    final String[] protocols = new String[] { "A" };
    defaults.setProtocols(protocols);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(protocols, params.getProtocols()));
  }
  
  @Test
  public void testPassDefaultCipherSuites() throws Exception {
    final String[] cipherSuites = new String[] { "A" };
    defaults.setCipherSuites(cipherSuites);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(Arrays.equals(cipherSuites, params.getCipherSuites()));
  }

  @Test
  public void testPassDefaultNeedClientAuth() throws Exception {
    defaults.setNeedClientAuth(true);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(params.getNeedClientAuth());
  }

  @Test
  public void testPassDefaultWantClientAuth() throws Exception {
    defaults.setWantClientAuth(true);
    SSLParameters params = factoryBean.createSSLParameters(defaults, context);
    assertTrue(params.getWantClientAuth());
  }

}
