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

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.net.JMSAppenderBase;

/**
 * A simple appender that publishes events to a JMS Queue. The events are
 * serialized and transmitted as JMS message type {@link
 * javax.jms.ObjectMessage}.
 * 
 * <p>
 * JMS {@link javax.jms.Queue queues} and
 * {@link javax.jms.QueueConnectionFactory queue connection factories} are
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
public class JMSQueueAppender extends JMSAppenderBase<LoggingEvent> {

  static int SUCCESSIVE_FAILURE_LIMIT = 3;

  String queueBindingName;
  String qcfBindingName;
  QueueConnection queueConnection;
  QueueSession queueSession;
  QueueSender queueSender;

  int successiveFailureCount = 0;

  public JMSQueueAppender() {
  }

  /**
   * The <b>QueueConnectionFactoryBindingName</b> option takes a string value.
   * Its value will be used to lookup the appropriate
   * <code>QueueConnectionFactory</code> from the JNDI context.
   */
  public void setQueueConnectionFactoryBindingName(String tcfBindingName) {
    this.qcfBindingName = tcfBindingName;
  }

  /**
   * Returns the value of the <b>QueueConnectionFactoryBindingName</b> option.
   */
  public String getQueueConnectionFactoryBindingName() {
    return qcfBindingName;
  }

  /**
   * The <b>QueueBindingName</b> option takes a string value. Its value will be
   * used to lookup the appropriate <code>Queue</code> from the JNDI context.
   */
  public void setQueueBindingName(String queueBindingName) {
    this.queueBindingName = queueBindingName;
  }

  /**
   * Returns the value of the <b>QueueBindingName</b> option.
   */
  public String getQueueBindingName() {
    return queueBindingName;
  }

  /**
   * Options are activated and become effective only after calling this method.
   */
  public void start() {
    QueueConnectionFactory queueConnectionFactory;

    try {
      Context jndi = buildJNDIContext();

      // addInfo("Looking up [" + qcfBindingName + "]");
      queueConnectionFactory = (QueueConnectionFactory) lookup(jndi,
          qcfBindingName);
      // addInfo("About to create QueueConnection.");
      if (userName != null) {
        this.queueConnection = queueConnectionFactory.createQueueConnection(
            userName, password);
      } else {
        this.queueConnection = queueConnectionFactory.createQueueConnection();
      }

      // addInfo(
      // "Creating QueueSession, non-transactional, "
      // + "in AUTO_ACKNOWLEDGE mode.");
      this.queueSession = queueConnection.createQueueSession(false,
          Session.AUTO_ACKNOWLEDGE);

      // addInfo("Looking up queue name [" + queueBindingName + "].");
      Queue queue = (Queue) lookup(jndi, queueBindingName);

      // addInfo("Creating QueueSender.");
      this.queueSender = queueSession.createSender(queue);

      // addInfo("Starting QueueConnection.");
      queueConnection.start();

      jndi.close();
    } catch (Exception e) {
      addError("Error while activating options for appender named [" + name
          + "].", e);
    }

    if (this.queueConnection != null && this.queueSession != null
        && this.queueSender != null) {
      super.start();
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
      if (queueSession != null) {
        queueSession.close();
      }
      if (queueConnection != null) {
        queueConnection.close();
      }
    } catch (Exception e) {
      addError("Error while closing JMSAppender [" + name + "].", e);
    }

    // Help garbage collection
    queueSender = null;
    queueSession = null;
    queueConnection = null;
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
      ObjectMessage msg = queueSession.createObjectMessage();
      msg.setObject(event);
      queueSender.send(msg);
      successiveFailureCount = 0;
    } catch (Exception e) {
      successiveFailureCount++;
      if (successiveFailureCount > SUCCESSIVE_FAILURE_LIMIT) {
        stop();
      }
      addError("Could not send message in JMSQueueAppender [" + name + "].", e);

    }
  }

  /**
   * Returns the QueueConnection used for this appender. Only valid after
   * start() method has been invoked.
   */
  protected QueueConnection getQueueConnection() {
    return queueConnection;
  }

  /**
   * Returns the QueueSession used for this appender. Only valid after start()
   * method has been invoked.
   */
  protected QueueSession getQueueSession() {
    return queueSession;
  }

  /**
   * Returns the QueueSender used for this appender. Only valid after start()
   * method has been invoked.
   */
  protected QueueSender getQueueSender() {
    return queueSender;
  }
}
