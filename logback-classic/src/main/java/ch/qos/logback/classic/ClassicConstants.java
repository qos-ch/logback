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
package ch.qos.logback.classic;

import static ch.qos.logback.core.CoreConstants.JNDI_JAVA_NAMESPACE;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClassicConstants {
    public static final String USER_MDC_KEY = "user";

    public static final String LOGBACK_CONTEXT_SELECTOR = "logback.ContextSelector";
    public static final String CONFIG_FILE_PROPERTY = "logback.configurationFile";

    /**
     * property name designating the path for the serialized configuration model file
     * @since 1.3.9/1.4.9
     */
    public static final String MODEL_CONFIG_FILE_PROPERTY = "logback.scmoFile";

    public static final String JNDI_CONFIGURATION_RESOURCE = JNDI_JAVA_NAMESPACE
            + "comp/env/logback/configuration-resource";
    public static final String JNDI_CONTEXT_NAME = JNDI_JAVA_NAMESPACE + "comp/env/logback/context-name";

    /** JNDI name of custom contextSelector classname, if used */
    public static final String JNDI_LOGBACK_CONTEXT_SELECTOR = JNDI_JAVA_NAMESPACE + "comp/env/logback/contextSelector";

    /**
     * The maximum number of package separators (dots) that abbreviation algorithms
     * can handle. Class or logger names with more separators will have their first
     * MAX_DOTS parts shortened.
     * 
     * Since 1.3.0, no longer unused
     */
    public static final int MAX_DOTS = 16;

    /**
     * The default stack data depth computed during caller data extraction.
     */
    public static final int DEFAULT_MAX_CALLEDER_DATA_DEPTH = 8;

    public static final String REQUEST_REMOTE_HOST_MDC_KEY = "req.remoteHost";
    public static final String REQUEST_USER_AGENT_MDC_KEY = "req.userAgent";
    public static final String REQUEST_REQUEST_URI = "req.requestURI";
    public static final String REQUEST_QUERY_STRING = "req.queryString";
    public static final String REQUEST_REQUEST_URL = "req.requestURL";
    public static final String REQUEST_METHOD = "req.method";
    public static final String REQUEST_X_FORWARDED_FOR = "req.xForwardedFor";

    public static final String GAFFER_CONFIGURATOR_FQCN = "ch.qos.logback.classic.gaffer.GafferConfigurator";

    public static final String FINALIZE_SESSION = "FINALIZE_SESSION";
    public static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker(FINALIZE_SESSION);
    final public static String AUTOCONFIG_FILE = "logback.xml";
    final public static String TEST_AUTOCONFIG_FILE = "logback-test.xml";

    public static final String LOGBACK_CLASSIC_VERSION_MESSAGE = "This is logback-classic version ";
    public static final String LOGBACK_VERSIONS_MISMATCH = "Versions of logback-core and logback-classic are different!";

}
