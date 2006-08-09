package ch.qos.logback.access.jetty;

import java.io.File;
import java.util.Iterator;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.StatusPrinter;


public class RequestLogImpl extends ContextBase implements RequestLog,
    AppenderAttachable {

  public final static String DEFAULT_CONFIG_FILE = "etc" + File.separatorChar
      + "logback.xml";

  AppenderAttachableImpl aai = new AppenderAttachableImpl();
  String filename;

  public void log(Request jettyRequest, Response jettyResponse) {
    AccessEvent accessEvent = new AccessEvent(jettyRequest, jettyResponse);
    // TODO better exception handling
    aai.appendLoopOnAppenders(accessEvent);
  }

  public void start() throws Exception {
    if (filename == null) {
      String jettyHomeProperty = System.getProperty("jetty.home");

      filename = jettyHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
      getStatusManager().add(
          new ErrorStatus("filename property not set. Assuming [" + filename
              + "]", this));

    }
    File configFile = new File(filename);
    if (configFile.exists()) {
      JoranConfigurator jc = new JoranConfigurator();
      jc.setContext(this);
      jc.doConfigure(filename);
      StatusPrinter.print(getStatusManager());
    } else {
      getStatusManager().add(
          new ErrorStatus("["+filename+"] does not exist", this));
    }

  }

  public void stop() throws Exception {
    System.out.println("RequestLogImpl-stop called");
    aai.detachAndStopAllAppenders();
  }

  public boolean isRunning() {
    System.out.println("RequestLogImpl-isRunning called");
    return false;
  }

  public void setFileName(String filename) {
    this.filename = filename;
  }

  public boolean isStarted() {
    System.out.println("RequestLogImpl-isStarted called");
    return true;
  }

  public boolean isStarting() {
    System.out.println("RequestLogImpl-isStarting called");
    return false;
  }

  public boolean isStopping() {
    System.out.println("RequestLogImpl-isStopping called");
    return false;
  }

  public boolean isFailed() {
    System.out.println("RequestLogImpl-isFailed called");
    return false;
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
}
