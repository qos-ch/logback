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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.SSLParameters;


/**
 * A factory bean for an {@link SSLParameters} object instance.
 * <p>
 * The primary function of this factory bean is to allow protocols and
 * cipher suites to be specified as a comma-separated string.
 *
 * @author Carl Harris
 */
public class SSLParametersFactoryBean {

  private String includedProtocols;
  private String excludedProtocols;
  private String includedCipherSuites;
  private String excludedCipherSuites;
  private Boolean needClientAuth;
  private Boolean wantClientAuth;

  /**
   * Creates an {@link SSLParameters} instance using the receiver's 
   * configuration.
   * @param defaults default SSL parameter values
   * @return parameters object
   */
  public SSLParameters createSSLParameters(SSLParameters defaults) {
    
    defaults.setProtocols(includedStrings(defaults.getProtocols(), 
          getIncludedProtocols(), getExcludedProtocols()));
    defaults.setCipherSuites(includedStrings(defaults.getCipherSuites(),
        getIncludedCipherSuites(), getExcludedCipherSuites()));
    if (isNeedClientAuth() != null) {
      defaults.setNeedClientAuth(isNeedClientAuth());
    }
    if (isWantClientAuth() != null) {
      defaults.setWantClientAuth(isWantClientAuth());
    }

    log(defaults);
    return defaults;
  }
  
  /**
   * Logs a parameters configuration.
   * @param params the subject parameters
   */
  private void log(SSLParameters params) {
    for (String protocol : params.getProtocols()) {
      SSL.logger.debug("enabled protocol: {}", protocol);
    }
    for (String cipherSuite : params.getCipherSuites()) {
      SSL.logger.debug("enabled cipher suite: {}", cipherSuite);
    }
    SSL.logger.info("client authentication: {}", 
        params.getNeedClientAuth() ? "required"
            : params.getWantClientAuth() ? "desired" : "none");
  }
  
  /**
   * Applies include and exclude patterns to an array of default string values
   * to produce an array of strings included by the patterns.
   * @param defaults default list of string values
   * @param included comma-separated patterns that identity values to include
   * @param excluded comma-separated patterns that identity string to exclude
   * @return an array of strings containing those strings from {@code defaults}
   *    that match at least one pattern in {@code included} that are not
   *    matched by any pattern in {@code excluded}
   */
  private String[] includedStrings(String[] defaults, String included,
      String excluded) {
    List<String> values = new ArrayList<String>(defaults.length);
    values.addAll(Arrays.asList(defaults));
    if (included != null) {
      retainMatching(values, stringToArray(included));
    }
    if (excluded != null) {
      removeMatching(values, stringToArray(excluded));
    }
    return values.toArray(new String[values.size()]);
  }
  
  /**
   * Retains all values in the subject collection that are matched by
   * at least one of a collection of regular expressions.
   * <p>
   * The semantics of this method are conceptually similar to
   * {@link Collection#retainAll(Collection)}, but uses pattern matching
   * instead of exact matching.
   * 
   * @param values subject value collection 
   * @param patterns patterns to match
   */
  private void retainMatching(Collection<String> values, String[] patterns) {
    List<String> matches = new ArrayList<String>(values.size());
    for (String p : patterns) {
      Pattern pattern = Pattern.compile(p);
      for (String value : values) {
        if (pattern.matcher(value).matches()) {
          matches.add(value);
        }
      }
    }
    values.retainAll(matches);
  }

  /**
   * Removes all values in the subject collection that are matched by
   * at least one of a collection of regular expressions.
   * <p>
   * The semantics of this method are conceptually similar to
   * {@link Collection#removeAll(Collection)}, but uses pattern matching
   * instead of exact matching.
   * 
   * @param values subject value collection 
   * @param patterns patterns to match
   */
  private void removeMatching(Collection<String> values, String[] patterns) {
    List<String> matches = new ArrayList<String>(values.size());
    for (String p : patterns) {
      Pattern pattern = Pattern.compile(p);
      for (String value : values) {
        if (pattern.matcher(value).matches()) {
          matches.add(value);
        }
      }
    }
    values.removeAll(matches);
  }

  /**
   * Splits a string containing comma-separated values into an array.
   * @param s the subject string
   * @return array of values contained in {@code s}
   */
  private String[] stringToArray(String s) {
    return s.split("\\s*,\\s*");
  }
  
  /**
   * Gets the JSSE secure transport protocols to include.
   * @return a string containing comma-separated JSSE secure transport 
   *    protocol names (e.g. {@code TLSv1})
   */
  public String getIncludedProtocols() {
    return includedProtocols;
  }

  /**
   * Sets the JSSE secure transport protocols to include.
   * @param protocols a string containing comma-separated JSSE secure 
   *    transport protocol names
   * @see Java Cryptography Architecture Standard Algorithm Name Documentation
   */
  public void setIncludedProtocols(String protocols) {
    this.includedProtocols = protocols;
  }

  /**
   * Gets the JSSE secure transport protocols to exclude.
   * @return a string containing comma-separated JSSE secure transport 
   *    protocol names (e.g. {@code TLSv1})
   */
  public String getExcludedProtocols() {
    return excludedProtocols;
  }

  /**
   * Sets the JSSE secure transport protocols to exclude.
   * @param protocols a string containing comma-separated JSSE secure 
   *    transport protocol names
   * @see Java Cryptography Architecture Standard Algorithm Name Documentation
   */
  public void setExcludedProtocols(String protocols) {
    this.excludedProtocols = protocols;
  }

  /**
   * Gets the JSSE cipher suite names to include.
   * @return a string containing comma-separated JSSE cipher suite names
   *    (e.g. {@code TLS_DHE_RSA_WITH_AES_256_CBC_SHA})
   */
  public String getIncludedCipherSuites() {
    return includedCipherSuites;
  }

  /**
   * Sets the JSSE cipher suite names to include.
   * @param cipherSuites a string containing comma-separated JSSE cipher
   *    suite names
   * @see Java Cryptography Architecture Standard Algorithm Name Documentation
   */
  public void setIncludedCipherSuites(String cipherSuites) {
    this.includedCipherSuites = cipherSuites;
  }

  /**
   * Gets the JSSE cipher suite names to exclude.
   * @return a string containing comma-separated JSSE cipher suite names
   *    (e.g. {@code TLS_DHE_RSA_WITH_AES_256_CBC_SHA})
   */
  public String getExcludedCipherSuites() {
    return excludedCipherSuites;
  }

  /**
   * Sets the JSSE cipher suite names to exclude.
   * @param cipherSuites a string containing comma-separated JSSE cipher
   *    suite names
   * @see Java Cryptography Architecture Standard Algorithm Name Documentation
   */
  public void setExcludedCipherSuites(String cipherSuites) {
    this.excludedCipherSuites = cipherSuites;
  }

  /**
   * Gets a flag indicating whether client authentication is required.
   * @return flag state
   */
  public Boolean isNeedClientAuth() {
    return needClientAuth;
  }

  /**
   * Sets a flag indicating whether client authentication is required.
   * @param needClientAuth the flag state to set
   */
  public void setNeedClientAuth(Boolean needClientAuth) {
    this.needClientAuth = needClientAuth;
  }

  /**
   * Gets a flag indicating whether client authentication is desired.
   * @return flag state
   */
  public Boolean isWantClientAuth() {
    return wantClientAuth;
  }

  /**
   * Sets a flag indicating whether client authentication is desired.
   * @param wantClientAuth the flag state to set
   */
  public void setWantClientAuth(Boolean wantClientAuth) {
    this.wantClientAuth = wantClientAuth;
  }

}
