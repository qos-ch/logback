package ch.qos.logback.access.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

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
 * added to the tomcat's server.xml:
 * 
 * &lt;Valve className="ch.qos.logback.access.tomcat.LogbackValve"/&gt;
 * 
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
 *     &lt;configuration&gt; 
 *       &lt;appender name=&quot;STDOUT&quot; class=&quot;ch.qos.logback.core.ConsoleAppender&quot;&gt; 
 *         &lt;layout class=&quot;ch.qos.logback.access.PatternLayout&quot;&gt; 
 *           &lt;param name=&quot;Pattern&quot; value=&quot;%date %server %remoteIP %clientHost %user %requestURL &quot; /&gt;
 *         &lt;/layout&gt; 
 *       &lt;/appender&gt; 
 *       
 *       &lt;appender-ref ref=&quot;STDOUT&quot; /&gt; 
 *     &lt;/configuration&gt;
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
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LogbackValve extends ContextBase implements Valve, Contained,
    AppenderAttachable {

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
}
