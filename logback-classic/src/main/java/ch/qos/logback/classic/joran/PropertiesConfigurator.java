/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2024, QOS.ch. All rights reserved.
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

package ch.qos.logback.classic.joran;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.JoranConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.model.util.VariableSubstitutionsHelper;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.ErrorCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static ch.qos.logback.core.CoreConstants.DOT;
import static ch.qos.logback.core.joran.JoranConstants.NULL;

public class PropertiesConfigurator extends ContextAwareBase {

    static Comparator<String> LENGTH_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            int len1 = o1 == null ? 0 : o1.length();
            int len2 = o2 == null ? 0 : o2.length();
            // longer strings first
            int diff = len2 - len1;
            if (diff != 0) {
                return diff;
            } else {
                return o2.compareTo(o1);
            }
        }
    };

    static final String LOGBACK_PREFIX = "logback";
    static final String LOGBACK_ROOT_LOGGER_PREFIX = LOGBACK_PREFIX + DOT + "root";
    static final int LOGBACK_ROOT_LOGGER_PREFIX_LENGTH = LOGBACK_ROOT_LOGGER_PREFIX.length();

    public static final String LOGBACK_LOGGER_PREFIX = LOGBACK_PREFIX + DOT + "logger" + DOT;
    static final int LOGBACK_LOGGER_PREFIX_LENGTH = LOGBACK_LOGGER_PREFIX.length();

    VariableSubstitutionsHelper variableSubstitutionsHelper;

    LoggerContext getLoggerContext() {
        return (LoggerContext) getContext();
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    public void doConfigure(URL url) throws JoranException {
        try {
            URLConnection urlConnection = url.openConnection();
            // per http://jira.qos.ch/browse/LOGBACK-117
            // per http://jira.qos.ch/browse/LOGBACK-163
            urlConnection.setUseCaches(false);
            InputStream in = urlConnection.getInputStream();
            doConfigure(in);
        } catch (IOException ioe) {
            String errMsg = "Could not open URL [" + url + "].";
            addError(errMsg, ioe);
            throw new JoranException(errMsg, ioe);
        }
    }

    public void doConfigure(File file) throws JoranException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            doConfigure(fileInputStream);
        } catch (IOException e) {
            throw new JoranException("Failed to load file " + file, e);
        }
    }

    public void doConfigure(String filename) throws JoranException {
        doConfigure(new File(filename));
    }

    public void doConfigure(InputStream inputStream) throws JoranException {
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new JoranException("Failed to load from input stream", e);
        } finally {
            close(inputStream);
        }

        doConfigure(props);
    }

    private void close(InputStream inputStream) throws JoranException {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new JoranException("failed to close stream", e);
            }
        }
    }

    void doConfigure(Properties properties) {
        Map<String, String> variablesMap = extractVariablesMap(properties);
        Map<String, String> instructionMap = extractLogbackInstructionMap(properties);

        this.variableSubstitutionsHelper = new VariableSubstitutionsHelper(context, variablesMap);
        configureLoggers(instructionMap);
        configureRootLogger(instructionMap);
    }

    void configureRootLogger(Map<String, String> instructionMap) {
        String val = subst(instructionMap.get(LOGBACK_ROOT_LOGGER_PREFIX));
        if (val != null) {
            setLevel(org.slf4j.Logger.ROOT_LOGGER_NAME, val);
        }
    }

    void configureLoggers(Map<String, String> instructionMap) {
        for (String key : instructionMap.keySet()) {
            if (key.startsWith(LOGBACK_LOGGER_PREFIX)) {
                String loggerName = key.substring(LOGBACK_LOGGER_PREFIX_LENGTH);
                String value = subst(instructionMap.get(key));
                setLevel(loggerName, value);
            }
        }
    }

    private void setLevel(String loggerName, String levelStr) {
        Logger logger = getLoggerContext().getLogger(loggerName);

        if (JoranConstants.INHERITED.equalsIgnoreCase(levelStr) || NULL.equalsIgnoreCase(levelStr)) {
            if (Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(loggerName)) {
                addError(ErrorCodes.ROOT_LEVEL_CANNOT_BE_SET_TO_NULL);
            } else {
                addInfo("Setting level of logger [" + loggerName + "] to null, i.e. INHERITED");
                logger.setLevel(null);
            }
        } else {
            Level level = Level.toLevel(levelStr);
            logger.setLevel(level);
        }
    }

    private Map<String, String> extractVariablesMap(Properties properties) {
        Map<String, String> variablesMap = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key != null && !key.startsWith(LOGBACK_PREFIX)) {
                variablesMap.put(key, properties.getProperty(key));
            }
        }

        return variablesMap;
    }

    private Map<String, String> extractLogbackInstructionMap(Properties properties) {
        Map<String, String> instructionMap = new TreeMap<>(LENGTH_COMPARATOR);
        for (String key : properties.stringPropertyNames()) {
            if (key != null && key.startsWith(LOGBACK_PREFIX)) {
                instructionMap.put(key, properties.getProperty(key));
            }
        }
        return instructionMap;
    }

    public String subst(String ref) {

        String substituted = variableSubstitutionsHelper.subst(ref);
        if (ref != null && !ref.equals(substituted)) {
            addInfo("value \"" + substituted + "\" substituted for \"" + ref + "\"");
        }
        return substituted;
    }

}
