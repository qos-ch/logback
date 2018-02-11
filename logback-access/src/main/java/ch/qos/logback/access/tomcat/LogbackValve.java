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
package ch.qos.logback.access.tomcat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import ch.qos.logback.access.AccessConstants;
import ch.qos.logback.access.joran.JoranConfigurator;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LifeCycleManager;
import ch.qos.logback.core.boolex.EventEvaluator;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.spi.LogbackLock;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.ExecutorServiceUtil;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;
import ch.qos.logback.core.util.StatusListenerConfigHelper;

//import org.apache.catalina.Lifecycle;

/**
 * This class is an implementation of tomcat's Valve interface, by extending
 * ValveBase.
 * 
 * <p>
 * For more information on using LogbackValve please refer to the online
 * documentation on <a
 * href="http://logback.qos.ch/access.html#tomcat">logback-acces and tomcat</a>.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class LogbackValve extends ValveBase implements Lifecycle, Context, AppenderAttachable<IAccessEvent>, FilterAttachable<IAccessEvent> {

    public final static String DEFAULT_FILENAME = "logback-access.xml";
    public final static String DEFAULT_CONFIG_FILE = "conf" + File.separatorChar + DEFAULT_FILENAME;
    final static String CATALINA_BASE_KEY = "catalina.base";
    final static String CATALINA_HOME_KEY = "catalina.home";

    private final LifeCycleManager lifeCycleManager = new LifeCycleManager();

    private long birthTime = System.currentTimeMillis();
   
    
    LogbackLock configurationLock = new LogbackLock();

    // Attributes from ContextBase:
    private String name;
    StatusManager sm = new BasicStatusManager();
    // TODO propertyMap should be observable so that we can be notified
    // when it changes so that a new instance of propertyMap can be
    // serialized. For the time being, we ignore this shortcoming.
    Map<String, String> propertyMap = new HashMap<String, String>();
    Map<String, Object> objectMap = new HashMap<String, Object>();
    private FilterAttachableImpl<IAccessEvent> fai = new FilterAttachableImpl<IAccessEvent>();

    AppenderAttachableImpl<IAccessEvent> aai = new AppenderAttachableImpl<IAccessEvent>();
    String filenameOption;
    boolean quiet;
    boolean started;
    boolean alreadySetLogbackStatusManager = false;
    private SequenceNumberGenerator sequenceNumberGenerator;
 

    private ScheduledExecutorService scheduledExecutorService;

    public LogbackValve() {
        putObject(CoreConstants.EVALUATOR_MAP, new HashMap<String, EventEvaluator<?>>());
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void startInternal() throws LifecycleException {
        scheduledExecutorService = ExecutorServiceUtil.newScheduledExecutorService();

        String filename;

        if (filenameOption != null) {
            filename = filenameOption;
        } else {
            addInfo("filename property not set. Assuming [" + DEFAULT_CONFIG_FILE + "]");
            filename = DEFAULT_CONFIG_FILE;
        }

        // String catalinaBase = OptionHelper.getSystemProperty(CATALINA_BASE_KEY);
        // String catalinaHome = OptionHelper.getSystemProperty(CATALINA_BASE_KEY);

        File configFile = searchForConfigFileTomcatProperty(filename, CATALINA_BASE_KEY);
        if (configFile == null) {
            configFile = searchForConfigFileTomcatProperty(filename, CATALINA_HOME_KEY);
        }

        URL resourceURL;
        if (configFile != null)
            resourceURL = fileToUrl(configFile);
        else
            resourceURL = searchAsResource(filename);

        if (resourceURL != null) {
            configureAsResource(resourceURL);
        } else {
            addWarn("Failed to find valid logback-access configuration file.");
        }

        if (!quiet) {
            StatusListenerConfigHelper.addOnConsoleListenerInstance(this, new OnConsoleStatusListener());
        }

        started = true;
        setState(LifecycleState.STARTING);
    }

    private URL fileToUrl(File configFile) {
        try {
            return configFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("File to URL conversion failed", e);
        }
    }

    private URL searchAsResource(String filename) {
        URL result = Loader.getResource(filename, getClass().getClassLoader());
        if (result != null)
            addInfo("Found [" + filename + "] as a resource.");
        else
            addInfo("Could NOT find [" + filename + "] as a resource.");
        return result;
    }

    private File searchForConfigFileTomcatProperty(String filename, String propertyKey) {
        String propertyValue = OptionHelper.getSystemProperty(propertyKey);
        String candidatePath = propertyValue + File.separatorChar + filename;
        if (propertyValue == null) {
            addInfo("System property \"" + propertyKey + "\" is not set. Skipping configuration file search with ${" + propertyKey + "} path prefix.");
            return null;
        }
        File candidateFile = new File(candidatePath);
        if (candidateFile.exists()) {
            addInfo("Found configuration file [" + candidatePath + "] using property \"" + propertyKey + "\"");
            return candidateFile;
        } else {
            addInfo("Could NOT configuration file [" + candidatePath + "] using property \"" + propertyKey + "\"");
            return null;
        }
    }

    public void addStatus(Status status) {
        StatusManager sm = getStatusManager();
        if (sm != null) {
            sm.add(status);
        }
    }

    public void addInfo(String msg) {
        addStatus(new InfoStatus(msg, this));
    }

    public void addWarn(String msg) {
        addStatus(new WarnStatus(msg, this));
    }

    public void addError(String msg, Throwable t) {
        addStatus(new ErrorStatus(msg, this, t));
    }

    private void configureAsResource(URL resourceURL) {
        try {
            JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(this);
            jc.doConfigure(resourceURL);
            addInfo("Done configuring");
        } catch (JoranException e) {
            addError("Failed to configure LogbackValve", e);
        }
    }

    public String getFilename() {
        return filenameOption;
    }

    public void setFilename(String filename) {
        this.filenameOption = filename;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            if (!alreadySetLogbackStatusManager) {
                alreadySetLogbackStatusManager = true;
                org.apache.catalina.Context tomcatContext = request.getContext();
                if (tomcatContext != null) {
                    ServletContext sc = tomcatContext.getServletContext();
                    if (sc != null) {
                        sc.setAttribute(AccessConstants.LOGBACK_STATUS_MANAGER_KEY, getStatusManager());
                    }
                }
            }

            getNext().invoke(request, response);

            TomcatServerAdapter adapter = new TomcatServerAdapter(request, response);
            IAccessEvent accessEvent = new AccessEvent(this, request, response, adapter);

            addThreadName(accessEvent);

            if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
                return;
            }

            // TODO better exception handling
            aai.appendLoopOnAppenders(accessEvent);
        } finally {
            request.removeAttribute(AccessConstants.LOGBACK_STATUS_MANAGER_KEY);
        }
    }

    private void addThreadName(IAccessEvent accessEvent) {
        try {
            final String threadName = Thread.currentThread().getName();
            if (threadName != null) {
                accessEvent.setThreadName(threadName);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        started = false;
        setState(LifecycleState.STOPPING);
        lifeCycleManager.reset();
        if (scheduledExecutorService != null) {
            ExecutorServiceUtil.shutdown(scheduledExecutorService);
            scheduledExecutorService = null;
        }
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

    public String getInfo() {
        return "Logback's implementation of ValveBase";
    }

    // Methods from ContextBase:
    @Override
    public StatusManager getStatusManager() {
        return sm;
    }

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    @Override
    public void putProperty(String key, String val) {
        this.propertyMap.put(key, val);
    }

    @Override
    public String getProperty(String key) {
        return (String) this.propertyMap.get(key);
    }

    @Override
    public Map<String, String> getCopyOfPropertyMap() {
        return new HashMap<String, String>(this.propertyMap);
    }

    @Override
    public Object getObject(String key) {
        return objectMap.get(key);
    }

    @Override
    public void putObject(String key, Object value) {
        objectMap.put(key, value);
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
    public ExecutorService getExecutorService() {
        return getScheduledExecutorService();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (this.name != null) {
            throw new IllegalStateException("LogbackValve has been already given a name");
        }
        this.name = name;
    }

    @Override
    public long getBirthTime() {
        return birthTime;
    }

    @Override
    public Object getConfigurationLock() {
        return configurationLock;
    }

    @Override
    public void register(LifeCycle component) {
        lifeCycleManager.register(component);
    }

    // ====== Methods from catalina Lifecycle =====

    @Override
    public void addLifecycleListener(LifecycleListener arg0) {
        // dummy NOP implementation
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }

    @Override
    public void removeLifecycleListener(LifecycleListener arg0) {
        // dummy NOP implementation
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append('[');
        sb.append(getName());
        sb.append(']');
        return sb.toString();
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Override
    public void addScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        throw new UnsupportedOperationException();
    }
    
    public SequenceNumberGenerator getSequenceNumberGenerator() {
        return sequenceNumberGenerator;
    }

    public void setSequenceNumberGenerator(SequenceNumberGenerator sequenceNumberGenerator) {
        this.sequenceNumberGenerator = sequenceNumberGenerator;
    }
}
