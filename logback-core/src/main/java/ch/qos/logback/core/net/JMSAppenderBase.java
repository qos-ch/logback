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
package ch.qos.logback.core.net;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import ch.qos.logback.core.AppenderBase;

/**
 * This class serves as a base class for 
 * JMSTopicAppender and JMSQueueAppender
 * 
 * For more information about this appender, please refer to:
 * http://logback.qos.ch/manual/appenders.html#JMSAppenderBase
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public abstract class JMSAppenderBase<E> extends AppenderBase<E> {

    protected String securityPrincipalName;
    protected String securityCredentials;
    protected String initialContextFactoryName;
    protected String urlPkgPrefixes;
    protected String providerURL;
    protected String userName;
    protected String password;

    protected Object lookup(Context ctx, String name) throws NamingException {
        try {
            return ctx.lookup(name);
        } catch (NameNotFoundException e) {
            addError("Could not find name [" + name + "].");
            throw e;
        }
    }

    public Context buildJNDIContext() throws NamingException {
        Context jndi = null;

        // addInfo("Getting initial context.");
        if (initialContextFactoryName != null) {
            Properties env = buildEnvProperties();
            jndi = new InitialContext(env);
        } else {
            jndi = new InitialContext();
        }
        return jndi;
    }

    public Properties buildEnvProperties() {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryName);
        if (providerURL != null) {
            env.put(Context.PROVIDER_URL, providerURL);
        } else {
            addWarn("You have set InitialContextFactoryName option but not the " + "ProviderURL. This is likely to cause problems.");
        }
        if (urlPkgPrefixes != null) {
            env.put(Context.URL_PKG_PREFIXES, urlPkgPrefixes);
        }

        if (securityPrincipalName != null) {
            env.put(Context.SECURITY_PRINCIPAL, securityPrincipalName);
            if (securityCredentials != null) {
                env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
            } else {
                addWarn("You have set SecurityPrincipalName option but not the " + "SecurityCredentials. This is likely to cause problems.");
            }
        }
        return env;
    }

    /**
     * Returns the value of the <b>InitialContextFactoryName</b> option. See
     * {@link #setInitialContextFactoryName} for more details on the meaning of
     * this option.
     */
    public String getInitialContextFactoryName() {
        return initialContextFactoryName;
    }

    /**
     * Setting the <b>InitialContextFactoryName</b> method will cause this
     * <code>JMSAppender</code> instance to use the {@link
     * InitialContext#InitialContext(Hashtable)} method instead of the no-argument
     * constructor. If you set this option, you should also at least set the
     * <b>ProviderURL</b> option.
     * 
     * <p>
     * See also {@link #setProviderURL(String)}.
     */
    public void setInitialContextFactoryName(String initialContextFactoryName) {
        this.initialContextFactoryName = initialContextFactoryName;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    public String getURLPkgPrefixes() {
        return urlPkgPrefixes;
    }

    public void setURLPkgPrefixes(String urlPkgPrefixes) {
        this.urlPkgPrefixes = urlPkgPrefixes;
    }

    public String getSecurityCredentials() {
        return securityCredentials;
    }

    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    public String getSecurityPrincipalName() {
        return securityPrincipalName;
    }

    public void setSecurityPrincipalName(String securityPrincipalName) {
        this.securityPrincipalName = securityPrincipalName;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * The user name to use when {@link
     * javax.jms.TopicConnectionFactory#createTopicConnection(String, String)}
     * creating a topic session}. If you set this option, you should also set the
     * <b>Password</b> option. See {@link #setPassword(String)}.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    /**
     * The password to use when creating a topic session.
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
