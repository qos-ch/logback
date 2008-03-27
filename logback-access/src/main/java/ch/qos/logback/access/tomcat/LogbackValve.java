package ch.qos.logback.access.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreGlobal;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * This class is an implementation of tomcat's Valve interface, by extending
 * ValveBase.
 * 
 * <p>For more information on using LogbackValve please refer to the online 
 * documentation on <a href="http://logback.qos.ch/access.html#tomcat">logback-acces and tomcat</a>.
 * 
 * <p>A special, module-specific implementation of PatternLayout was implemented to
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
public class LogbackValve extends ValveBase implements Lifecycle, Context,
    AppenderAttachable<AccessEvent>, FilterAttachable {

  public final static String DEFAULT_CONFIG_FILE = "conf" + File.separatorChar
      + "logback-access.xml";

  // Attributes from ContextBase:
  private String name;
  StatusManager sm = new BasicStatusManager();
  // TODO propertyMap should be observable so that we can be notified
  // when it changes so that a new instance of propertyMap can be
  // serialized. For the time being, we ignore this shortcoming.
  Map<String, String> propertyMap = new HashMap<String, String>();
  Map<String, Object> objectMap = new HashMap<String, Object>();
  private FilterAttachableImpl fai = new FilterAttachableImpl();

  AppenderAttachableImpl<AccessEvent> aai = new AppenderAttachableImpl<AccessEvent>();
  String filename;
  boolean quiet;
  boolean started;

  public LogbackValve() {
    putObject(CoreGlobal.EVALUATOR_MAP, new HashMap());
  }

  public void start() {
    if (filename == null) {
      String tomcatHomeProperty = System.getProperty("catalina.home");

      filename = tomcatHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
      getStatusManager().add(
          new InfoStatus("filename property not set. Assuming [" + filename
              + "]", this));
    }
    File configFile = new File(filename);
    if (configFile.exists()) {
      try {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(this);
        jc.doConfigure(filename);
      } catch (JoranException e) {
        // TODO can we do better than printing a stack trace on syserr?
        e.printStackTrace();
      }
    } else {
      getStatusManager().add(
          new WarnStatus("[" + filename + "] does not exist", this));
    }

    if(!quiet) {
      StatusPrinter.print(getStatusManager());
    }

    started = true;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
     this.filename = filename;
  }
  
  public boolean isQuiet() {
    return quiet;
  }

  public void setQuiet(boolean quiet) {
    this.quiet = quiet;
  }

  public void invoke(Request request, Response response) throws IOException,
      ServletException {

    getNext().invoke(request, response);

    TomcatServerAdapter adapter = new TomcatServerAdapter(request, response);
    AccessEvent accessEvent = new AccessEvent(request, response, adapter);

    if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
      return;
    }

    // TODO better exception handling
    aai.appendLoopOnAppenders(accessEvent);
  }

  public void stop() {
    started = false;
  }

  public void addAppender(Appender<AccessEvent> newAppender) {
    aai.addAppender(newAppender);
  }

  public Iterator iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public Appender<AccessEvent> getAppender(String name) {
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

  public Appender<AccessEvent> detachAppender(String name) {
    return aai.detachAppender(name);
  }

  public String getInfo() {
    return "Logback's implementation of ValveBase";
  }

  // Methods from ContextBase:
  public StatusManager getStatusManager() {
    return sm;
  }

  public Map<String, String> getPropertyMap() {
    return propertyMap;
  }

  public void putProperty(String key, String val) {
    this.propertyMap.put(key, val);
  }

  public String getProperty(String key) {
    return (String) this.propertyMap.get(key);
  }

  public Object getObject(String key) {
    return objectMap.get(key);
  }

  public void putObject(String key, Object value) {
    objectMap.put(key, value);
  }

  public void addFilter(Filter newFilter) {
    fai.addFilter(newFilter);
  }

  public Filter getFirstFilter() {
    return fai.getFirstFilter();
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public FilterReply getFilterChainDecision(Object event) {
    return fai.getFilterChainDecision(event);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (this.name != null) {
      throw new IllegalStateException(
          "LogbackValve has been already given a name");
    }
    this.name = name;
  }



  // Methods from catalina Lifecycle

  public void addLifecycleListener(LifecycleListener arg0) {
    // dummy NOP implementation
  }

  public LifecycleListener[] findLifecycleListeners() {
    return new LifecycleListener[0];
  }

  public void removeLifecycleListener(LifecycleListener arg0) {
    // dummy NOP implementation
  }

}
