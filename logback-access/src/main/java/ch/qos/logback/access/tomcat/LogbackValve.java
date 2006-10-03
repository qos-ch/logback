package ch.qos.logback.access.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ContainerBase;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * This class is an implementation of tomcat's Valve interface.
 * 
 * It can be seen as logback classic's LoggerContext. Appenders can be attached
 * directly to LogbackValve and LogbackValve uses the same StatusManager as
 * LoggerContext does. It also provides containers for properties.
 * <p>
 * To configure tomcat in order to use LogbackValve, the following lines must be
 * added to the tomcat's server.xml, nested in an <code>Engine</code> element:
 * <p>
 * &lt;Valve className="ch.qos.logback.access.tomcat.LogbackValve"/&gt;
 * <p>
 * By default, LogbackValve looks for a logback configuration file called
 * logback.xml, in the same folder where the tomcat configuration is located,
 * that is /conf/logback.xml. The logback.xml file is slightly different than
 * the usual logback classic configuration file. Most of it is the same:
 * Appenders and Layouts are declared the exact same way. However, loggers
 * elements are not allowed.
 * <p>
 * Here is a sample logback.xml file that can be used right away:
 * 
 * <pre>
 *        &lt;configuration&gt; 
 *          &lt;appender name=&quot;STDOUT&quot; class=&quot;ch.qos.logback.core.ConsoleAppender&quot;&gt; 
 *            &lt;layout class=&quot;ch.qos.logback.access.PatternLayout&quot;&gt; 
 *              &lt;param name=&quot;Pattern&quot; value=&quot;%date %server %remoteIP %clientHost %user %requestURL &quot; /&gt;
 *            &lt;/layout&gt; 
 *          &lt;/appender&gt; 
 *          
 *          &lt;appender-ref ref=&quot;STDOUT&quot; /&gt; 
 *        &lt;/configuration&gt;
 * </pre>
 * 
 * A special, module-specific implementation of PatternLayout was implemented to
 * allow http-specific patterns to be used. The
 * {@link ch.qos.logback.access.PatternLayout} provides a way to format the
 * logging output that is just as easy and flexible as the usual PatternLayout.
 * For more information about the general use of a PatternLayout, please refer
 * to logback classic's {@link ch.qos.logback.classic.PatternLayout}. For
 * information about logback access' specific PatternLayout, please refer to
 * it's javadoc.
 * <p>
 * 
 * <p>
 * MBean registration parts of this class come from tomcat's ValveBase
 * implementation.
 * <p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LogbackValve extends ContextBase implements Valve, Contained,
    AppenderAttachable, MBeanRegistration {

  public final static String DEFAULT_CONFIG_FILE = "conf" + File.separatorChar
      + "logback.xml";

  AppenderAttachableImpl aai = new AppenderAttachableImpl();
  String filename;
  boolean started;

  Container container;
  Valve nextValve;

  public LogbackValve() {
    // System.out.println("LogbackValve constructor called");
    start();
  }

  public void start() {
    if (filename == null) {
      String tomcatHomeProperty = System.getProperty("catalina.home");

      filename = tomcatHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
      getStatusManager().add(
          new ErrorStatus("filename property not set. Assuming [" + filename
              + "]", this));

    }
    File configFile = new File(filename);
    if (configFile.exists()) {
      JoranConfigurator jc = new JoranConfigurator();
      jc.setContext(this);
      jc.doConfigure(filename);
      // StatusPrinter.print(getStatusManager());
    } else {
      getStatusManager().add(
          new ErrorStatus("[" + filename + "] does not exist", this));
    }
    started = true;
  }

  public void invoke(Request request, Response response) throws IOException,
      ServletException {

    nextValve.invoke(request, response);

    // System.out.println("**** LogbackValve invoke called");
    TomcatServerAdapter adapter = new TomcatServerAdapter(request, response);
    AccessEvent accessEvent = new AccessEvent(request, response, adapter);
    // TODO better exception handling
    aai.appendLoopOnAppenders(accessEvent);
  }

  public void stop() {
    started = false;
  }

  public void addAppender(Appender newAppender) {
    aai.addAppender(newAppender);
  }

  public Iterator iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public Appender getAppender(String name) {
    return aai.getAppender(name);
  }

  public boolean isAttached(Appender appender) {
    return aai.isAttached(appender);
  }

  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();

  }

  public boolean detachAppender(Appender appender) {
    return aai.detachAppender(appender);
  }

  public Appender detachAppender(String name) {
    return aai.detachAppender(name);
  }

  public void backgroundProcess() {
  }

  public String getInfo() {
    return "logback's valve";
  }

  public Valve getNext() {
    return nextValve;
  }

  public void setNext(Valve next) {
    this.nextValve = next;
  }

  public void setFileName(String fileName) {
    this.filename = fileName;
  }

  public Container getContainer() {
    return container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  // -------------------- JMX and Registration --------------------
  // MBean descriptions for custom components needed
  // in order to avoid a "ManagedBean is not found" exception.

  protected String domain;
  protected ObjectName oname;
  protected MBeanServer mserver;
  protected ObjectName controller;

  public ObjectName getObjectName() {
    return oname;
  }

  public void setObjectName(ObjectName oname) {
    this.oname = oname;
  }

  public String getDomain() {
    return domain;
  }

  public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception {
    oname = name;
    mserver = server;
    domain = name.getDomain();

    return name;
  }

  public void postRegister(Boolean registrationDone) {
  }

  public void preDeregister() throws Exception {
  }

  public void postDeregister() {
  }

  public ObjectName getController() {
    return controller;
  }

  public void setController(ObjectName controller) {
    this.controller = controller;
  }

  /**
   * From the name, extract the parent object name
   * 
   * @param valveName
   *          The valve name
   * @return ObjectName The parent name
   */
  public ObjectName getParentName(ObjectName valveName) {

    return null;
  }

  public ObjectName createObjectName(String domain, ObjectName parent)
      throws MalformedObjectNameException {
    Container container = this.getContainer();
    if (container == null || !(container instanceof ContainerBase))
      return null;
    ContainerBase containerBase = (ContainerBase) container;
    Pipeline pipe = containerBase.getPipeline();
    Valve valves[] = pipe.getValves();

    /* Compute the "parent name" part */
    String parentName = "";
    if (container instanceof Engine) {
    } else if (container instanceof Host) {
      parentName = ",host=" + container.getName();
    } else if (container instanceof Context) {
      String path = ((Context) container).getPath();
      if (path.length() < 1) {
        path = "/";
      }
      Host host = (Host) container.getParent();
      parentName = ",path=" + path + ",host=" + host.getName();
    } else if (container instanceof Wrapper) {
      Context ctx = (Context) container.getParent();
      String path = ctx.getPath();
      if (path.length() < 1) {
        path = "/";
      }
      Host host = (Host) ctx.getParent();
      parentName = ",servlet=" + container.getName() + ",path=" + path
          + ",host=" + host.getName();
    }

    String className = this.getClass().getName();
    int period = className.lastIndexOf('.');
    if (period >= 0)
      className = className.substring(period + 1);

    int seq = 0;
    for (int i = 0; i < valves.length; i++) {
      // Find other valves with the same name
      if (valves[i] == this) {
        break;
      }
      if (valves[i] != null && valves[i].getClass() == this.getClass()) {

        seq++;
      }
    }
    String ext = "";
    if (seq > 0) {
      ext = ",seq=" + seq;
    }

    ObjectName objectName = new ObjectName(domain + ":type=Valve,name="
        + className + ext + parentName);
    return objectName;
  }

  // -------------------- JMX data --------------------

  public ObjectName getContainerName() {
    if (container == null)
      return null;
    return ((ContainerBase) container).getJmxName();
  }
}
