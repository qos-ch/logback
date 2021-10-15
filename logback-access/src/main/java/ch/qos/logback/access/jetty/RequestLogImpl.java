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
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.util.FileUtil;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusPrinter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.LifeCycle;

/**
 * This class is logback's implementation of jetty's RequestLog interface.
 * <p>
 * It can be seen as logback classic's LoggerContext. Appenders can be attached
 * directly to RequestLogImpl and RequestLogImpl uses the same StatusManager as
 * LoggerContext does. It also provides containers for properties.
 * </p>
 * <h2>Supported Jetty Versions</h2>
 * <p>
 *     This {@code RequestLogImpl} only supports Jetty 7.0.0 through Jetty 10.
 *     If you are using Jetty 11 with the new Jakarta Servlets (namespace {@code jakarta.servlet})
 *     then you will need a more modern version of {@code logback-access}.
 * </p>
 * <h2>Configuring for Jetty 9.4.x through to Jetty 10.0.x</h2>
 * <p>
 *     Jetty 9.4.x and Jetty 10.x use a modern @{code server.setRequestLog(RequestLog)} interface that
 *     is based on a Server level RequestLog behavior.  This means all requests are logged,
 *     even bad requests, and context-less requests.  The internals of the Jetty Request and
 *     Response objects track the state of the object at the time they are committed (the
 *     actual state during the application when an action on the network commits the
 *     request/response exchange).  This prevents behaviors from 3rd party libraries
 *     that change the state of the request / response before the RequestLog gets a chance
 *     to log the details.  This differs from Jetty 9.3.x and
 *     older in that those versions used a (now deprecated) {@code RequestLogHandler} and
 *     would never see bad requests, or context-less requests,
 *     and if a 3rd party library modifies the the response (for example by setting
 *     {@code response.setStatus(200)} after the response has been initiated on the network)
 *     this change in status would be logged, instead of the actual status that was sent.
 * </p>
 * <p>
 *     First, you must be using the proper {@code ${jetty.home}} and {@code ${jetty.base}}
 *     directory split.  Configure your {@code ${jetty.base}} with at least the `resources` module
 *     enabled (so that your configuration can be found).
 * </p>
 * <p>
 *     Next, create a {@code ${jetty.base}/etc/logback-access-requestlog.xml} file with the following
 *     content.
 * </p>
 * <pre>
 *   &lt;?xml version="1.0"?&gt;
 *   &lt;!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_3.dtd"&gt;
 *
 *   &lt;Configure id="Server" class="org.eclipse.jetty.server.Server"&gt;
 *     &lt;Set name="requestLog"&gt;
 *       &lt;New id="LogbackAccess" class="ch.qos.logback.access.jetty.RequestLogImpl"&gt;
 *         &lt;Set name="resource"&gt;logback-access.xml&lt;/Set&gt;
 *       &lt;/New&gt;
 *     &lt;/Set&gt;
 *   &lt;/Configure&gt;
 * </pre>
 * <p/>
 * <p>
 *     Now you'll need a {@code ${jetty.base}/resources/logback-access.xml} configuration file.
 * </p>
 * By default, {@code RequestLogImpl} looks for a logback configuration file called
 * {@code etc/logback-access.xml}, in the {@code ${jetty.base}} directory, then
 * the older {@code ${jetty.home}} directory.
 * The {@code logback-access.xml} file is slightly
 * different than the usual logback classic configuration file. Most of it is
 * the same: Appenders and Layouts are declared the exact same way. However,
 * loggers elements are not allowed. <p> It is possible to put the logback
 * configuration file anywhere, as long as it's path is specified. Here is
 * another example, with a path to the logback-access.xml file.
 * <p/>
 * <pre>
 *   &lt;?xml version="1.0"?&gt;
 *   &lt;!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_3.dtd"&gt;
 *
 *   &lt;Configure id="Server" class="org.eclipse.jetty.server.Server"&gt;
 *     &lt;Set name="requestLog"&gt;
 *       &lt;New id="LogbackAccess" class="ch.qos.logback.access.jetty.RequestLogImpl"&gt;
 *         &lt;Set name="fileName"&gt;/path/to/logback-access.xml&lt;/Set&gt;
 *       &lt;/New&gt;
 *     &lt;/Set&gt;
 *   &lt;/Configure&gt;
 * </pre>
 * <h2>Configuring for Jetty 7.x thru to Jetty 9.3.x</h2>
 * <p>
 * To configure these older Jetty instances to use {@code RequestLogImpl},
 * the use of the {@code RequestLogHandler} is the technique available to you.
 * Modify your {@code etc/jetty-requestlog.xml}
 * </p>
 * <pre>
 *   &lt;?xml version="1.0"?&gt;
 *   &lt;!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd"&gt;
 *
 *   &lt;Configure id="Server" class="org.eclipse.jetty.server.Server"&gt;
 *     &lt;Ref id="Handlers"&gt;
 *       &lt;Call name="addHandler"&gt;
 *         &lt;Arg&gt;
 *           &lt;New id="RequestLog" class="org.eclipse.jetty.server.handler.RequestLogHandler"&gt;
 *             &lt;Set name="requestLog"&gt;
 *               &lt;New id="RequestLogImpl" class="ch.qos.logback.access.jetty.RequestLogImpl"/&gt;
 *             &lt;/Set&gt;
 *           &lt;/New&gt;
 *         &lt;/Arg&gt;
 *       &lt;/Call&gt;
 *     &lt;/Ref&gt;
 *   &lt;/Configure&gt;
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
 *   &lt;?xml version="1.0"?&gt;
 *   &lt;!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd"&gt;
 *
 *   &lt;Configure id="Server" class="org.eclipse.jetty.server.Server"&gt;
 *     &lt;Ref id="Handlers"&gt;
 *       &lt;Call name="addHandler"&gt;
 *         &lt;Arg&gt;
 *           &lt;New id="RequestLog" class="org.eclipse.jetty.server.handler.RequestLogHandler"&gt;
 *             &lt;Set name="requestLog"&gt;
 *               &lt;New id="RequestLogImpl" class="ch.qos.logback.access.jetty.RequestLogImpl"&gt;
 *                 &lt;Set name="fileName"&gt;path/to/logback-access.xml&lt;/Set&gt;
 *               &lt;/New&gt;
 *             &lt;/Set&gt;
 *           &lt;/New&gt;
 *         &lt;/Arg&gt;
 *       &lt;/Call&gt;
 *     &lt;/Ref&gt;
 *   &lt;/Configure&gt;
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
public class RequestLogImpl extends ContextBase implements RequestLog, LifeCycle, AppenderAttachable<IAccessEvent>, FilterAttachable<IAccessEvent> {
    public final static String DEFAULT_CONFIG_FILE = "etc" + File.separatorChar + "logback-access.xml";

    enum State {
        FAILED, STOPPED, STARTING, STARTED, STOPPING
    }
    State state = State.STOPPED;

    AppenderAttachableImpl<IAccessEvent> aai = new AppenderAttachableImpl<IAccessEvent>();
    FilterAttachableImpl<IAccessEvent> fai = new FilterAttachableImpl<IAccessEvent>();
    String fileName;
    String resource;

    // Jetty 9.4.x and newer is considered modern.
    boolean modernJettyRequestLog;
    boolean quiet = false;

    public RequestLogImpl() {
        putObject(CoreConstants.EVALUATOR_MAP, new HashMap<String, EventEvaluator<?>>());

        // plumb the depths of Jetty and the environment ...
        if (classIsPresent("jakarta.servlet.http.HttpServlet")) {
            throw new RuntimeException("The new jakarta.servlet classes are not supported by this " +
                "version of logback-access (check for a newer version of logback-access that " +
                "does support it)");
        }

        // look for modern approach to RequestLog
        modernJettyRequestLog = classIsPresent("org.eclipse.jetty.server.CustomRequestLog");
    }

    private boolean classIsPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e)  {
            return false;
        }
    }

    @Override
    public void log(Request jettyRequest, Response jettyResponse) {
        JettyServerAdapter adapter = newJettyServerAdapter(jettyRequest, jettyResponse);
        IAccessEvent accessEvent = new AccessEvent(jettyRequest, jettyResponse, adapter);
        if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
            return;
        }
        aai.appendLoopOnAppenders(accessEvent);
    }

    private JettyServerAdapter newJettyServerAdapter(Request jettyRequest, Response jettyResponse) {
        if (modernJettyRequestLog) {
            return new JettyModernServerAdapter(jettyRequest, jettyResponse);
        } else {
            return new JettyServerAdapter(jettyRequest, jettyResponse);
        }
    }

    protected void addInfo(String msg) {
        getStatusManager().add(new InfoStatus(msg, this));
    }

    private void addError(String msg) {
        getStatusManager().add(new ErrorStatus(msg, this));
    }

    @Override
    public void start() {
        state = State.STARTING;
        try {
            configure();
            if (!isQuiet()) {
                StatusPrinter.print(getStatusManager());
            }
            state = State.STARTED;
        } catch(Throwable t) {
            t.printStackTrace();
            state = State.FAILED;
        }
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

        String defaultConfigFile = DEFAULT_CONFIG_FILE;
        // Always attempt ${jetty.base} first
        String jettyBaseProperty = OptionHelper.getSystemProperty("jetty.base");
        if (!OptionHelper.isEmpty(jettyBaseProperty)) {
            defaultConfigFile = jettyBaseProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
        }

        File file = new File(defaultConfigFile);
        if(!file.exists()) {
            // Then use ${jetty.home} (not supported in Jetty 10+)
            String jettyHomeProperty = OptionHelper.getSystemProperty("jetty.home");
            if (!OptionHelper.isEmpty(jettyHomeProperty)) {
                defaultConfigFile = jettyHomeProperty + File.separatorChar + DEFAULT_CONFIG_FILE;
            } else {
                addInfo("Neither [jetty.base] nor [jetty.home] system properties are set.");
            }
        }

        file = new File(defaultConfigFile);
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
        state = State.STOPPING;
        aai.detachAndStopAllAppenders();
        state = State.STOPPED;
    }

    @Override
    public boolean isRunning() {
        return state == State.STARTED;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean isStarted() {
        return state == State.STARTED;
    }

    @Override
    public boolean isStarting() {
        return state == State.STARTING;
    }

    @Override
    public boolean isStopping() {
        return state == State.STOPPING;
    }

    public boolean isStopped() {
        return state == State.STOPPED;
    }

    @Override
    public boolean isFailed() {
        return state == State.FAILED;
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
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        // we'll implement this when asked
    }

    @Override
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        // we'll implement this when asked
    }
}
