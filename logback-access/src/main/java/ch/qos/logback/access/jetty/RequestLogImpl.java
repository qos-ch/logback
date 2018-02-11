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
package ch.qos.logback.access.jetty;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.util.FileUtil;
import ch.qos.logback.core.util.StatusPrinter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;

import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.util.OptionHelper;

/**
 * This class is logback's implementation of jetty's RequestLog interface. <p>
 * It can be seen as logback classic's LoggerContext. Appenders can be attached
 * directly to RequestLogImpl and RequestLogImpl uses the same StatusManager as
 * LoggerContext does. It also provides containers for properties. <p> To
 * configure jetty in order to use RequestLogImpl, the following lines must be
 * added to the jetty configuration file, namely <em>etc/jetty.xml</em>:
 * <p/>
 * <pre>
 *    &lt;Ref id=&quot;requestLog&quot;&gt;
 *      &lt;Set name=&quot;requestLog&quot;&gt;
 *        &lt;New id=&quot;requestLogImpl&quot; class=&quot;ch.qos.logback.access.jetty.RequestLogImpl&quot;&gt;&lt;/New&gt;
 *      &lt;/Set&gt;
 *    &lt;/Ref&gt;
 * </pre>
 * <p/>
 * By default, RequestLogImpl looks for a logback configuration file called
 * logback-access.xml, in the same folder where jetty.xml is located, that is
 * <em>etc/logback-access.xml</em>. The logback-access.xml file is slightly
 * different than the usual logback classic configuration file. Most of it is
 * the same: Appenders and Layouts are declared the exact same way. However,
 * loggers elements are not allowed. <p> It is possible to put the logback
 * configuration file anywhere, as long as it's path is specified. Here is
 * another example, with a path to the logback-access.xml file.
 * <p/>
 * <pre>
 *    &lt;Ref id=&quot;requestLog&quot;&gt;
 *      &lt;Set name=&quot;requestLog&quot;&gt;
 *        &lt;New id=&quot;requestLogImpl&quot; class=&quot;ch.qos.logback.access.jetty.RequestLogImpl&quot;&gt;&lt;/New&gt;
 *          &lt;Set name=&quot;fileName&quot;&gt;path/to/logback.xml&lt;/Set&gt;
 *      &lt;/Set&gt;
 *    &lt;/Ref&gt;
 * </pre>
 * <p/>
 * <p> Here is a sample logback-access.xml file that can be used right away:
 * <p/>
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
 * <p/>
 * <p> Another configuration file, using SMTPAppender, could be:
 * <p/>
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
public class RequestLogImpl extends ContextBase implements RequestLog, AppenderAttachable<IAccessEvent>, FilterAttachable<IAccessEvent> {

    public final static String DEFAULT_CONFIG_FILE = "etc" + File.separatorChar + "logback-access.xml";

    AppenderAttachableImpl<IAccessEvent> aai = new AppenderAttachableImpl<IAccessEvent>();
    FilterAttachableImpl<IAccessEvent> fai = new FilterAttachableImpl<IAccessEvent>();
    String fileName;
    String resource;
    boolean started = false;
    boolean quiet = false;

    public RequestLogImpl() {
        putObject(CoreConstants.EVALUATOR_MAP, new HashMap<String, EventEvaluator<?>>());
    }

    @Override
    public void log(Request jettyRequest, Response jettyResponse) {
        JettyServerAdapter adapter = new JettyServerAdapter(jettyRequest, jettyResponse);
        IAccessEvent accessEvent = new AccessEvent(this, jettyRequest, jettyResponse, adapter);
        if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
            return;
        }
        aai.appendLoopOnAppenders(accessEvent);
    }

    private void addInfo(String msg) {
        getStatusManager().add(new InfoStatus(msg, this));
    }

    private void addError(String msg) {
        getStatusManager().add(new ErrorStatus(msg, this));
    }

    @Override
    public void start() {
        configure();
        if (!isQuiet()) {
            StatusPrinter.print(getStatusManager());
        }
        started = true;
    }

    protected void configure() {
        URL configURL = getConfigurationFileURL();
        if (configURL != null) {
            runJoranOnFile(configURL);
        } else {
            addError("Could not find configuration file for logback-access");
        }
    }

    protected URL getConfigurationFileURL() {
        if (fileName != null) {
            addInfo("Will use configuration file [" + fileName + "]");
            File file = new File(fileName);
            if (!file.exists())
                return null;
            return FileUtil.fileToURL(file);
        }
        if (resource != null) {
            addInfo("Will use configuration resource [" + resource + "]");
            return this.getClass().getResource(resource);
        }

        String jettyHomeProperty = OptionHelper.getSystemProperty("jetty.home");
        String defaultConfigFile = DEFAULT_CONFIG_FILE;
        if (!OptionHelper.isEmpty(jettyHomeProperty)) {
            defaultConfigFile = jettyHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
        } else {
            addInfo("[jetty.home] system property not set.");
        }
        File file = new File(defaultConfigFile);
        addInfo("Assuming default configuration file [" + defaultConfigFile + "]");
        if (!file.exists())
            return null;
        return FileUtil.fileToURL(file);
    }

    private void runJoranOnFile(URL configURL) {
        try {
            JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(this);
            jc.doConfigure(configURL);
            if (getName() == null) {
                setName("LogbackRequestLog");
            }
        } catch (JoranException e) {
            // errors have been registered as status messages
        }
    }

    @Override
    public void stop() {
        aai.detachAndStopAllAppenders();
        started = false;
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isStarting() {
        return false;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return !started;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public void addAppender(Appender<IAccessEvent> newAppender) {
        aai.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<IAccessEvent>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<IAccessEvent> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<IAccessEvent> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<IAccessEvent> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    @Override
    public void addFilter(Filter<IAccessEvent> newFilter) {
        fai.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        fai.clearAllFilters();
    }

    @Override
    public List<Filter<IAccessEvent>> getCopyOfAttachedFiltersList() {
        return fai.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(IAccessEvent event) {
        return fai.getFilterChainDecision(event);
    }

    @Override
    public void addLifeCycleListener(Listener listener) {
        // we'll implement this when asked
    }

    @Override
    public void removeLifeCycleListener(Listener listener) {
        // we'll implement this when asked
    }

}
