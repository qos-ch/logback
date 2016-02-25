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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import ch.qos.logback.core.util.LocationUtil;

/**
 * A factory bean for a JCA {@link KeyStore}.
 * <p>
 * This object holds the configurable properties of a key store and uses
 * them to create and load a {@link KeyStore} instance.
 *
 * @author Carl Harris
 */
public class KeyStoreFactoryBean {

    private String location;
    private String provider;
    private String type;
    private String password;

    /**
     * Creates a new {@link KeyStore} using the receiver's configuration.
     * @return key store
     * @throws NoSuchProviderException if the provider specified by 
     *    {@link #setProvider(String)} is not known to the platform
     * @throws NoSuchAlgorithmException if the key store type specified by
     *    {@link #setType(String)} is not known to the specified provider
     *    (or the platform's default provider if the provider isn't specified)
     * @throws KeyStoreException if some other error occurs in loading
     *    the key store from the resource specified by 
     *    {@link #setLocation(String)}
     */
    public KeyStore createKeyStore() throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException {

        if (getLocation() == null) {
            throw new IllegalArgumentException("location is required");
        }

        InputStream inputStream = null;
        try {
            URL url = LocationUtil.urlForResource(getLocation());
            inputStream = url.openStream();
            KeyStore keyStore = newKeyStore();
            keyStore.load(inputStream, getPassword().toCharArray());
            return keyStore;
        } catch (NoSuchProviderException ex) {
            throw new NoSuchProviderException("no such keystore provider: " + getProvider());
        } catch (NoSuchAlgorithmException ex) {
            throw new NoSuchAlgorithmException("no such keystore type: " + getType());
        } catch (FileNotFoundException ex) {
            throw new KeyStoreException(getLocation() + ": file not found");
        } catch (Exception ex) {
            throw new KeyStoreException(getLocation() + ": " + ex.getMessage(), ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * Invokes the appropriate JCE factory method to obtain a new
     * {@link KeyStore} object.
     */
    private KeyStore newKeyStore() throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException {

        return getProvider() != null ? KeyStore.getInstance(getType(), getProvider()) : KeyStore.getInstance(getType());
    }

    /**
     * Gets the location of the key store resource.
     * @return a String containing a URL for the resource
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the key store resource.
     * @param location a String containing a URL for the resource; if the 
     *    URL string isn't prefixed by a scheme, the path is assumed to be 
     *    relative to the root of the classpath.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the type of key store to load.
     * @return a key store type name (e.g. {@code JKS}); the 
     *    {@link SSL#DEFAULT_KEYSTORE_TYPE} is returned if no type has been configured
     */
    public String getType() {
        if (type == null) {
            return SSL.DEFAULT_KEYSTORE_TYPE;
        }
        return type;
    }

    /**
     * Sets the type of key store to load.
     * @param type a key store type name (e.g. {@code JKS}, {@code PKCS12});
     *    the type specified must be supported by the provider specified by
     *    {@link #setProvider(String)} or by the platform's default provider
     *    if no provider is specified
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the JCA key store provider name.
     * @return provider name or {@code null} if no provider has been configured
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the JCA key store provider name.
     * @param provider name of the JCA provider to utilize in creating the
     *    key store
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Gets the password to use to access the key store.
     * @return password string; the {@link SSL#DEFAULT_KEYSTORE_PASSWORD} is returned
     *    if no password has been configured
     */
    public String getPassword() {
        if (password == null) {
            return SSL.DEFAULT_KEYSTORE_PASSWORD;
        }
        return password;
    }

    /**
     * Sets the password to use to access the keystore.
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
