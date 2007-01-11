package ch.qos.logback.classic.net;

import java.util.Properties;

import javax.jms.ObjectMessage;
import javax.naming.Context;

import junit.framework.TestCase;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.net.mock.MockInitialContext;
import ch.qos.logback.classic.net.mock.MockInitialContextFactory;
import ch.qos.logback.classic.net.mock.MockQueue;
import ch.qos.logback.classic.net.mock.MockQueueConnectionFactory;
import ch.qos.logback.classic.net.mock.MockQueueSender;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ContextBase;

public class JMSQueueAppenderTest extends TestCase {

  ch.qos.logback.core.Context context;
  JMSQueueAppender appender;

  @Override
  protected void setUp() throws Exception {
    context = new ContextBase();
    appender = new JMSQueueAppender();
    appender.setContext(context);
    appender.setName("jmsQueue");
    appender.qcfBindingName = "queueCnxFactory";
    appender.queueBindingName = "testQueue";
    appender.providerURL = "url";
    appender.initialContextFactoryName = MockInitialContextFactory.class.getName();
    
    MockInitialContext mic = MockInitialContextFactory.getContext();
    mic.map.put(appender.qcfBindingName, new MockQueueConnectionFactory());
    mic.map.put(appender.queueBindingName, new MockQueue(appender.queueBindingName));
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    appender = null;
    context = null;
    super.tearDown();
  }

  public void testAppendOk() { 
    appender.start();

    LoggingEvent le = createLoggingEvent();
    appender.append(le);
    
    MockQueueSender qs = (MockQueueSender)appender.queueSender;
    assertEquals(1, qs.getMessageList().size());
    ObjectMessage message = (ObjectMessage) qs.getMessageList().get(0);
    try {
      assertEquals(le, message.getObject());
    } catch (Exception e) {
      fail();
    }
  }

  public void testAppendFailure() {
    appender.start();
    
    //make sure the append method does not work
    appender.queueSender = null;
    
    LoggingEvent le = createLoggingEvent();
    for (int i = 1; i <= 3; i++) {
      appender.append(le);
      assertEquals(i, context.getStatusManager().getCount());
      assertTrue(appender.isStarted());
    }
    appender.append(le);
    assertEquals(4, context.getStatusManager().getCount());
    assertFalse(appender.isStarted());
  }

  public void testBuildEnvProperties() {
    appender.initialContextFactoryName = "icfn";
    appender.providerURL = "url";
    appender.urlPkgPrefixes = "pkgPref";
    appender.securityPrincipalName = "user";
    appender.securityCredentials = "cred";

    Properties props = appender.buildEnvProperties();
    assertEquals(5, props.size());
    assertEquals(appender.initialContextFactoryName, props
        .getProperty(Context.INITIAL_CONTEXT_FACTORY));
    assertEquals(appender.providerURL, props.getProperty(Context.PROVIDER_URL));
    assertEquals(appender.urlPkgPrefixes, props
        .getProperty(Context.URL_PKG_PREFIXES));
    assertEquals(appender.securityPrincipalName, props
        .getProperty(Context.SECURITY_PRINCIPAL));
    assertEquals(appender.securityCredentials, props
        .getProperty(Context.SECURITY_CREDENTIALS));
  }

  public void testBuildEnvPropertiesWithNullProviderURL() {
    appender.initialContextFactoryName = "icfn";
    appender.providerURL = null;
    appender.urlPkgPrefixes = "pkgPref";
    appender.securityPrincipalName = "user";
    appender.securityCredentials = "cred";

    Properties props = appender.buildEnvProperties();
    assertEquals(4, props.size());
    assertEquals(appender.initialContextFactoryName, props
        .getProperty(Context.INITIAL_CONTEXT_FACTORY));
    assertEquals(null, props.getProperty(Context.PROVIDER_URL));
    assertEquals(appender.urlPkgPrefixes, props
        .getProperty(Context.URL_PKG_PREFIXES));
    assertEquals(appender.securityPrincipalName, props
        .getProperty(Context.SECURITY_PRINCIPAL));
    assertEquals(appender.securityCredentials, props
        .getProperty(Context.SECURITY_CREDENTIALS));

    assertEquals(1, context.getStatusManager().getCount());
  }

  public void testBuildEnvPropertiesWithNullCredentials() {
    appender.initialContextFactoryName = "icfn";
    appender.providerURL = "url";
    appender.urlPkgPrefixes = "pkgPref";
    appender.securityPrincipalName = "user";
    appender.securityCredentials = null;

    Properties props = appender.buildEnvProperties();
    assertEquals(4, props.size());
    assertEquals(appender.initialContextFactoryName, props
        .getProperty(Context.INITIAL_CONTEXT_FACTORY));
    assertEquals(appender.providerURL, props.getProperty(Context.PROVIDER_URL));
    assertEquals(appender.urlPkgPrefixes, props
        .getProperty(Context.URL_PKG_PREFIXES));
    assertEquals(appender.securityPrincipalName, props
        .getProperty(Context.SECURITY_PRINCIPAL));
    assertEquals(null, props
        .getProperty(Context.SECURITY_CREDENTIALS));

    assertEquals(1, context.getStatusManager().getCount());
  }
  
  public void testBuildEnvPropertiesWithPkgNull() {
    appender.initialContextFactoryName = "icfn";
    appender.providerURL = "url";
    appender.urlPkgPrefixes = null;
    appender.securityPrincipalName = "user";
    appender.securityCredentials = "cred";

    Properties props = appender.buildEnvProperties();
    assertEquals(4, props.size());
    assertEquals(appender.initialContextFactoryName, props
        .getProperty(Context.INITIAL_CONTEXT_FACTORY));
    assertEquals(appender.providerURL, props.getProperty(Context.PROVIDER_URL));
    assertEquals(null, props
        .getProperty(Context.URL_PKG_PREFIXES));
    assertEquals(appender.securityPrincipalName, props
        .getProperty(Context.SECURITY_PRINCIPAL));
    assertEquals(appender.securityCredentials, props
        .getProperty(Context.SECURITY_CREDENTIALS));

    assertEquals(0, context.getStatusManager().getCount());
  }

  public void testStartMinimalInfo() {
    //let's leave only what's in the setup()
    //method, minus the providerURL
    appender.providerURL = null;
    appender.start();
    
    assertTrue(appender.isStarted());
    
    try {
      assertEquals(appender.queueBindingName, appender.queueSender.getQueue().getQueueName());
    } catch (Exception e) {
      fail();
    }
  }
  
  public void testStartUserPass() {
    appender.userName = "";
    appender.password = "";
    
    appender.start();
    
    assertTrue(appender.isStarted());
    
    try {
      assertEquals(appender.queueBindingName, appender.queueSender.getQueue().getQueueName());
    } catch (Exception e) {
      fail();
    }
  }
  
  public void testStartFails() {
    appender.queueBindingName = null;
    
    appender.start();
    
    assertFalse(appender.isStarted());
  }

  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent();
    le.setLevel(Level.DEBUG);
    le.setMessage("test message");
    le.setTimeStamp(System.currentTimeMillis());
    le.setThreadName(Thread.currentThread().getName());
    return le;
  }
}
