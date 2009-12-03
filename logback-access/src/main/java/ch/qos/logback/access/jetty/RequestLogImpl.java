/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.access.jetty;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.OptionHelper;

/**
 * This class is logback's implementation of jetty's RequestLog interface. <p>
 * It can be seen as logback classic's LoggerContext. Appenders can be attached
 * directly to RequestLogImpl and RequestLogImpl uses the same StatusManager as
 * LoggerContext does. It also provides containers for properties. <p> To
 * configure jetty in order to use RequestLogImpl, the following lines must be
 * added to the jetty configuration file, namely <em>etc/jetty.xml</em>:
 * 
 * <pre>
 *    &lt;Ref id=&quot;requestLog&quot;&gt; 
 *      &lt;Set name=&quot;requestLog&quot;&gt; 
 *        &lt;New id=&quot;requestLogImpl&quot; class=&quot;ch.qos.logback.access.jetty.RequestLogImpl&quot;&gt;&lt;/New&gt;
 *      &lt;/Set&gt; 
 *    &lt;/Ref&gt;
 * </pre>
 * 
 * By default, RequestLogImpl looks for a logback configuration file called
 * logback-access.xml, in the same folder where jetty.xml is located, that is
 * <em>etc/logback-access.xml</em>. The logback-access.xml file is slightly
 * different than the usual logback classic configuration file. Most of it is
 * the same: Appenders and Layouts are declared the exact same way. However,
 * loggers elements are not allowed. <p> It is possible to put the logback
 * configuration file anywhere, as long as it's path is specified. Here is
 * another example, with a path to the logback-access.xml file.
 * 
 * <pre>
 *    &lt;Ref id=&quot;requestLog&quot;&gt; 
 *      &lt;Set name=&quot;requestLog&quot;&gt; 
 *        &lt;New id=&quot;requestLogImpl&quot; class=&quot;ch.qos.logback.access.jetty.RequestLogImpl&quot;&gt;&lt;/New&gt;
 *          &lt;Set name=&quot;fileName&quot;&gt;path/to/logback.xml&lt;/Set&gt;
 *      &lt;/Set&gt; 
 *    &lt;/Ref&gt;
 * </pre>
 * 
 * <p> Here is a sample logback-access.xml file that can be used right away:
 * 
 * <pre>
 *    &lt;configuration&gt; 
 *      &lt;appender name=&quot;STDOUT&quot; class=&quot;ch.qos.logback.core.ConsoleAppender&quot;&gt; 
 *        &lt;layout class=&quot;ch.qos.logback.access.PatternLayout&quot;&gt; 
 *          &lt;param name=&quot;Pattern&quot; value=&quot;%date %server %remoteIP %clientHost %user %requestURL&quot; /&gt;
 *        &lt;/layout&gt; 
 *      &lt;/appender&gt; 
 *      
 *      &lt;appender-ref ref=&quot;STDOUT&quot; /&gt; 
 *    &lt;/configuration&gt;
 * </pre>
 * 
 * <p> Another configuration file, using SMTPAppender, could be:
 * 
 * <pre>
 *    &lt;configuration&gt; 
 *      &lt;appender name=&quot;SMTP&quot; class=&quot;ch.qos.logback.access.net.SMTPAppender&quot;&gt;
 *        &lt;layout class=&quot;ch.qos.logback.access.PatternLayout&quot;&gt;
 *          &lt;param name=&quot;pattern&quot; value=&quot;%remoteIP [%date] %requestURL %statusCode %bytesSent&quot; /&gt;
 *        &lt;/layout&gt;
 *        &lt;param name=&quot;From&quot; value=&quot;sender@domaine.org&quot; /&gt;
 *        &lt;param name=&quot;SMTPHost&quot; value=&quot;mail.domain.org&quot; /&gt;
 *         &lt;param name=&quot;Subject&quot; value=&quot;Last Event: %statusCode %requestURL&quot; /&gt;
 *         &lt;param name=&quot;To&quot; value=&quot;server_admin@domain.org&quot; /&gt;
 *      &lt;/appender&gt;
 *      &lt;appender-ref ref=&quot;SMTP&quot; /&gt; 
 *    &lt;/configuration&gt;
 * </pre>
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class RequestLogImpl extends ContextBase implements RequestLog,
    AppenderAttachable<AccessEvent>, FilterAttachable<AccessEvent> {

  public final static String DEFAULT_CONFIG_FILE = "etc" + File.separatorChar
      + "logback-access.xml";

  AppenderAttachableImpl<AccessEvent> aai = new AppenderAttachableImpl<AccessEvent>();
  FilterAttachableImpl<AccessEvent> fai = new FilterAttachableImpl<AccessEvent>();
  String filename;
  boolean started = false;

  public RequestLogImpl() {
    putObject(CoreConstants.EVALUATOR_MAP, new HashMap());
  }

  public void log(Request jettyRequest, Response jettyResponse) {
    JettyServerAdapter adapter = new JettyServerAdapter(jettyRequest,
        jettyResponse);
    AccessEvent accessEvent = new AccessEvent(jettyRequest, jettyResponse,
        adapter);
    if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
      return;
    }
    aai.appendLoopOnAppenders(accessEvent);
  }

  public void start() {
    if (filename == null) {
      String jettyHomeProperty = OptionHelper.getSystemProperty("jetty.home");

      filename = jettyHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
      getStatusManager().add(
          new WarnStatus("filename property not set. Assuming [" + filename
              + "]", this));

    }
    try {
      File configFile = new File(filename);
      if (configFile.exists()) {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(this);
        jc.doConfigure(filename);

      } else {
        getStatusManager().add(
            new ErrorStatus("[" + filename + "] does not exist", this));
      }

      if (getName() == null) {
        setName("LogbackRequestLog");
      }
      RequestLogRegistry.register(this);
      getStatusManager().add(
          new InfoStatus("RequestLog added to RequestLogRegistry with name: "
              + getName(), this));

      started = true;

    } catch (JoranException e) {
      // errors have been registered as status messages
    }
  }

  public void stop() {
    aai.detachAndStopAllAppenders();
    started = false;
  }

  public boolean isRunning() {
    return started;
  }

  public void setFileName(String filename) {
    this.filename = filename;
  }

  public boolean isStarted() {
    return started;
  }

  public boolean isStarting() {
    return false;
  }

  public boolean isStopping() {
    return false;
  }

  public boolean isStopped() {
    return !started;
  }

  public boolean isFailed() {
    return false;
  }

  public void addAppender(Appender<AccessEvent> newAppender) {
    aai.addAppender(newAppender);
  }

  public Iterator<Appender<AccessEvent>> iteratorForAppenders() {
    return aai.iteratorForAppenders();
  }

  public Appender<AccessEvent> getAppender(String name) {
    return aai.getAppender(name);
  }

  public boolean isAttached(Appender<AccessEvent> appender) {
    return aai.isAttached(appender);
  }

  public void detachAndStopAllAppenders() {
    aai.detachAndStopAllAppenders();

  }

  public boolean detachAppender(Appender<AccessEvent> appender) {
    return aai.detachAppender(appender);
  }

  public boolean detachAppender(String name) {
    return aai.detachAppender(name);
  }

  public void addFilter(Filter<AccessEvent> newFilter) {
    fai.addFilter(newFilter);
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public List<Filter<AccessEvent>> getCopyOfAttachedFiltersList() {
    return fai.getCopyOfAttachedFiltersList();
  }
  
  public FilterReply getFilterChainDecision(AccessEvent event) {
    return fai.getFilterChainDecision(event);
  }

  public Filter getFirstFilter() {
    return fai.getFirstFilter();
  }
}
