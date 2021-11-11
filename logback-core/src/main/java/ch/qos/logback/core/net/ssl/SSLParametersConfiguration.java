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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLEngine;

import org.codehaus.janino.Java;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StringCollectionUtil;

/**
 * A configuration of SSL parameters for an {@link SSLEngine}.
 *
 * @author Carl Harris
 * @author Bruno Harbulot 
 */
public class SSLParametersConfiguration extends ContextAwareBase {

    private String includedProtocols;
    private String excludedProtocols;
    private String includedCipherSuites;
    private String excludedCipherSuites;
    private Boolean needClientAuth;
    private Boolean wantClientAuth;
    private String[] enabledProtocols;
    private String[] enabledCipherSuites;
    private Boolean hostnameVerification;
    
    /**
     * Configures SSL parameters on an {@link SSLConfigurable}.
     * @param socket the subject configurable
     */
    public void configure(SSLConfigurable socket) {
        socket.setEnabledProtocols(enabledProtocols(socket.getSupportedProtocols(), socket.getDefaultProtocols()));
        socket.setEnabledCipherSuites(enabledCipherSuites(socket.getSupportedCipherSuites(), socket.getDefaultCipherSuites()));
        if (isNeedClientAuth() != null) {
            socket.setNeedClientAuth(isNeedClientAuth());
        }
        if (isWantClientAuth() != null) {
            socket.setWantClientAuth(isWantClientAuth());
        }
        if(hostnameVerification != null) {
        	addInfo("hostnameVerification="+hostnameVerification);
            socket.setHostnameVerification(hostnameVerification);
        }
    }

 
    public boolean getHostnameVerification() {
    	if(hostnameVerification == null)
    		return false;
    	return hostnameVerification;
    }

    public void setHostnameVerification(boolean hostnameVerification) {
    	this.hostnameVerification = hostnameVerification;
    }
    
    /**
     * Gets the set of enabled protocols based on the configuration.
     * @param supportedProtocols protocols supported by the SSL engine 
     * @param defaultProtocols default protocols enabled by the SSL engine
     * @return enabled protocols
     */
    private String[] enabledProtocols(String[] supportedProtocols, String[] defaultProtocols) {
        if (enabledProtocols == null) {
            // we're assuming that the same engine is used for all configurables
            // so once we determine the enabled set, we won't do it again
            if (OptionHelper.isNullOrEmpty(getIncludedProtocols()) && OptionHelper.isNullOrEmpty(getExcludedProtocols())) {
                enabledProtocols = Arrays.copyOf(defaultProtocols, defaultProtocols.length);
            } else {
                enabledProtocols = includedStrings(supportedProtocols, getIncludedProtocols(), getExcludedProtocols());
            }
            for (String protocol : enabledProtocols) {
                addInfo("enabled protocol: " + protocol);
            }
        }
        return enabledProtocols;
    }

    /**
     * Gets the set of enabled cipher suites based on the configuration.
     * @param supportedCipherSuites cipher suites supported by the SSL engine 
     * @param defaultCipherSuites default cipher suites enabled by the SSL engine
     * @return enabled cipher suites
     */
    private String[] enabledCipherSuites(String[] supportedCipherSuites, String[] defaultCipherSuites) {
        if (enabledCipherSuites == null) {
            // we're assuming that the same engine is used for all configurables
            // so once we determine the enabled set, we won't do it again
            if (OptionHelper.isNullOrEmpty(getIncludedCipherSuites()) && OptionHelper.isNullOrEmpty(getExcludedCipherSuites())) {
                enabledCipherSuites = Arrays.copyOf(defaultCipherSuites, defaultCipherSuites.length);
            } else {
                enabledCipherSuites = includedStrings(supportedCipherSuites, getIncludedCipherSuites(), getExcludedCipherSuites());
            }
            for (String cipherSuite : enabledCipherSuites) {
                addInfo("enabled cipher suite: " + cipherSuite);
            }
        }
        return enabledCipherSuites;
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
    private String[] includedStrings(String[] defaults, String included, String excluded) {
        List<String> values = new ArrayList<String>(defaults.length);
        values.addAll(Arrays.asList(defaults));
        if (included != null) {
            StringCollectionUtil.retainMatching(values, stringToArray(included));
        }
        if (excluded != null) {
            StringCollectionUtil.removeMatching(values, stringToArray(excluded));
        }
        return values.toArray(new String[values.size()]);
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
