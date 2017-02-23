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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * A factory bean for a JCA {@link SecureRandom} generator.
 * <p>
 * This object holds the configurable properties of a secure random generator
 * and uses them to create and load a {@link SecureRandom} instance.
 *
 * @author Carl Harris
 */
public class SecureRandomFactoryBean {

    private String algorithm;
    private String provider;

    /**
     * Creates a new {@link SecureRandom} generator using the receiver's 
     * configuration.
     * @return secure random generator instance
     * @throws NoSuchProviderException if the provider name specified by
     *    {@link #setProvider(String)} is not known to the platform
     * @throws NoSuchAlgorithmException if the algorithm name specified by
     *    {@link #setAlgorithm(String)} is not recognized by the specified
     *    provider (or the platform's default provider if the provider isn't 
     *    specified)
     */
    public SecureRandom createSecureRandom() throws NoSuchProviderException, NoSuchAlgorithmException {
        try {
            return getProvider() != null ? SecureRandom.getInstance(getAlgorithm(), getProvider()) : SecureRandom.getInstance(getAlgorithm());
        } catch (NoSuchProviderException ex) {
            throw new NoSuchProviderException("no such secure random provider: " + getProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException("no such secure random algorithm: " + getAlgorithm());
        }
    }

    /**
     * Gets the secure random generator algorithm name. 
     * @return an algorithm name (e.g. {@code SHA1PRNG}); the 
     *    {@link SSL#DEFAULT_SECURE_RANDOM_ALGORITHM} is returned if no algorithm has been
     *    specified
     */
    public String getAlgorithm() {
        if (algorithm == null) {
            return SSL.DEFAULT_SECURE_RANDOM_ALGORITHM;
        }
        return algorithm;
    }

    /**
     * Sets the secure random generator algorithm name.
     * @param algorithm an algorithm name, which must be recognized by the
     *    provider specified via {@link #setProvider(String)} or by the
     *    platform's default provider if no provider is specified.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Gets the JCA provider name for the secure random generator.
     * @return provider name
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the JCA provider name for the secure random generator.
     * @param provider name of the JCA provider to utilize in creating the
     *    secure random generator
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

}
