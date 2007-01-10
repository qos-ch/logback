/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.classic.net;

import java.util.Hashtable;
import java.util.Properties;

import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A simple appender that publishes events to a JMS Topic. The events are
 * serialized and transmitted as JMS message type {@link
 * javax.jms.ObjectMessage}.
 * 
 * <p>
 * JMS {@link javax.jms.Topic topics} and
 * {@link javax.jms.TopicConnectionFactory topic connection factories} are
 * administered objects that are retrieved using JNDI messaging which in turn
 * requires the retreival of a JNDI {@link Context}.
 * 
 * <p>
 * There are two common methods for retrieving a JNDI {@link Context}. If a
 * file resource named <em>jndi.properties</em> is available to the JNDI API,
 * it will use the information found therein to retrieve an initial JNDI
 * context. To obtain an initial context, your code will simply call:
 * 
 * <pre>
 * InitialContext jndiContext = new InitialContext();
 * </pre>
 * 
 * <p>
 * Calling the no-argument <code>InitialContext()</code> method will also work
 * from within Enterprise Java Beans (EJBs) because it is part of the EJB
 * contract for application servers to provide each bean an environment naming
 * context (ENC).
 * 
 * <p>
 * In the second approach, several predetermined properties are set and these
 * properties are passed to the <code>InitialContext</code> contructor to
 * connect to the naming service provider. For example, to connect to JBoss
 * naming service one would write:
 * 
 * <pre>
 * Properties env = new Properties();
 * env.put(Context.INITIAL_CONTEXT_FACTORY,
 *     &quot;org.jnp.interfaces.NamingContextFactory&quot;);
 * env.put(Context.PROVIDER_URL, &quot;jnp://hostname:1099&quot;);
 * env.put(Context.URL_PKG_PREFIXES, &quot;org.jboss.naming:org.jnp.interfaces&quot;);
 * InitialContext jndiContext = new InitialContext(env);
 * </pre>
 * 
 * where <em>hostname</em> is the host where the JBoss applicaiton server is
 * running.
 * 
 * <p>
 * To connect to the the naming service of Weblogic application server one would
 * write:
 * 
 * <pre>
 * Properties env = new Properties();
 * env.put(Context.INITIAL_CONTEXT_FACTORY,
 *     &quot;weblogic.jndi.WLInitialContextFactory&quot;);
 * env.put(Context.PROVIDER_URL, &quot;t3://localhost:7001&quot;);
 * InitialContext jndiContext = new InitialContext(env);
 * </pre>
 * 
 * <p>
 * Other JMS providers will obviously require different values.
 * 
 * The initial JNDI context can be obtained by calling the no-argument
 * <code>InitialContext()</code> method in EJBs. Only clients running in a
 * separate JVM need to be concerned about the <em>jndi.properties</em> file
 * and calling {@link InitialContext#InitialContext()} or alternatively
 * correctly setting the different properties before calling {@link
 * InitialContext#InitialContext(java.util.Hashtable)} method.
 * 
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class JMSTopicAppender extends AppenderBase<LoggingEvent> {

  static int SUCCESSIVE_FAILURE_LIMIT = 3;

  String securityPrincipalName;
  String securityCredentials;
  String initialContextFactoryName;
  String urlPkgPrefixes;
  String providerURL;
  String topicBindingName;
  String tcfBindingName;
  String userName;
  String password;
  TopicConnection topicConnection;
  TopicSession topicSession;
  TopicPublisher topicPublisher;

  int successiveFailureCount = 0;

  public JMSTopicAppender() {
  }

  /**
   * The <b>TopicConnectionFactoryBindingName</b> option takes a string value.
   * Its value will be used to lookup the appropriate
   * <code>TopicConnectionFactory</code> from the JNDI context.
   */
  public void setTopicConnectionFactoryBindingName(String tcfBindingName) {
    this.tcfBindingName = tcfBindingName;
  }

  /**
   * Returns the value of the <b>TopicConnectionFactoryBindingName</b> option.
   */
  public String getTopicConnectionFactoryBindingName() {
    return tcfBindingName;
  }

  /**
   * The <b>TopicBindingName</b> option takes a string value. Its value will be
   * used to lookup the appropriate <code>Topic</code> from the JNDI context.
   */
  public void setTopicBindingName(String topicBindingName) {
    this.topicBindingName = topicBindingName;
  }

  /**
   * Returns the value of the <b>TopicBindingName</b> option.
   */
  public String getTopicBindingName() {
    return topicBindingName;
  }

  /**
   * Options are activated and become effective only after calling this method.
   */
  public void start() {
    TopicConnectionFactory topicConnectionFactory;

    try {
      Context jndi = buildJNDIContext();

      // addInfo("Looking up [" + tcfBindingName + "]");
      topicConnectionFactory = (TopicConnectionFactory) lookup(jndi,
          tcfBindingName);
      // addInfo("About to create TopicConnection.");
      if (userName != null) {
        this.topicConnection = topicConnectionFactory.createTopicConnection(
            userName, password);
      } else {
        this.topicConnection = topicConnectionFactory.createTopicConnection();
      }

      // addInfo(
      // "Creating TopicSession, non-transactional, "
      // + "in AUTO_ACKNOWLEDGE mode.");
      this.topicSession = topicConnection.createTopicSession(false,
          Session.AUTO_ACKNOWLEDGE);

      // addInfo("Looking up topic name [" + topicBindingName + "].");
      Topic topic = (Topic) lookup(jndi, topicBindingName);

      // addInfo("Creating TopicPublisher.");
      this.topicPublisher = topicSession.createPublisher(topic);

      // addInfo("Starting TopicConnection.");
      topicConnection.start();

      jndi.close();
    } catch (Exception e) {
      addError("Error while activating options for appender named [" + name
          + "].", e);
    }

    if (this.topicConnection != null && this.topicSession != null
        && this.topicPublisher != null) {
      super.start();
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
      addWarn("You have set InitialContextFactoryName option but not the "
          + "ProviderURL. This is likely to cause problems.");
    }
    if (urlPkgPrefixes != null) {
      env.put(Context.URL_PKG_PREFIXES, urlPkgPrefixes);
    }

    if (securityPrincipalName != null) {
      env.put(Context.SECURITY_PRINCIPAL, securityPrincipalName);
      if (securityCredentials != null) {
        env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
      } else {
        addWarn("You have set SecurityPrincipalName option but not the "
            + "SecurityCredentials. This is likely to cause problems.");
      }
    }
    return env;
  }

  protected Object lookup(Context ctx, String name) throws NamingException {
    try {
      return ctx.lookup(name);
    } catch (NameNotFoundException e) {
      addError("Could not find name [" + name + "].");
      throw e;
    }
  }

  /**
   * Close this JMSAppender. Closing releases all resources used by the
   * appender. A closed appender cannot be re-opened.
   */
  public synchronized void stop() {
    // The synchronized modifier avoids concurrent append and close operations
    if (!this.started) {
      return;
    }

    this.started = false;

    try {
      if (topicSession != null) {
        topicSession.close();
      }
      if (topicConnection != null) {
        topicConnection.close();
      }
    } catch (Exception e) {
      addError("Error while closing JMSAppender [" + name + "].", e);
    }

    // Help garbage collection
    topicPublisher = null;
    topicSession = null;
    topicConnection = null;
  }


  /**
   * This method called by {@link AppenderSkeleton#doAppend} method to do most
   * of the real appending work.
   */
  public void append(LoggingEvent event) {
    if (!isStarted()) {
      return;
    }

    try {
      ObjectMessage msg = topicSession.createObjectMessage();

      msg.setObject(event);
      topicPublisher.publish(msg);
      successiveFailureCount = 0;
    } catch (Exception e) {
      successiveFailureCount++;
      if (successiveFailureCount > SUCCESSIVE_FAILURE_LIMIT) {
        stop();
      }
      addError("Could not publish message in JMSTopicAppender [" + name + "].", e);
    }
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

  String getURLPkgPrefixes() {
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
   * The paswword to use when creating a topic session.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the TopicConnection used for this appender. Only valid after
   * start() method has been invoked.
   */
  protected TopicConnection getTopicConnection() {
    return topicConnection;
  }

  /**
   * Returns the TopicSession used for this appender. Only valid after start()
   * method has been invoked.
   */
  protected TopicSession getTopicSession() {
    return topicSession;
  }

  /**
   * Returns the TopicPublisher used for this appender. Only valid after start()
   * method has been invoked.
   */
  protected TopicPublisher getTopicPublisher() {
    return topicPublisher;
  }
}
