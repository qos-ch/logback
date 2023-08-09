/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2023, QOS.ch. All rights reserved.
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

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.joran.serializedModel.HardenedModelInputStream;
import ch.qos.logback.classic.model.processor.LogbackClassicDefaultNestedComponentRules;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ModelUtil;
import ch.qos.logback.core.model.processor.DefaultProcessor;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.Loader;
import ch.qos.logback.core.util.OptionHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static ch.qos.logback.core.CoreConstants.MODEL_CONFIG_FILE_EXTENSION;

/**
 * @since 1.3.9/1.4.9
 */

// BEWARE: the fqcn is used in SerializedModelModelHandler
@ConfiguratorRank(value = ConfiguratorRank.SERIALIZED_MODEL)
public class SerializedModelConfigurator extends ContextAwareBase implements Configurator {

    final public static String AUTOCONFIG_MODEL_FILE = "logback"+ MODEL_CONFIG_FILE_EXTENSION;

    final public static String TEST_AUTOCONFIG_MODEL_FILE = "logback-test"+ MODEL_CONFIG_FILE_EXTENSION;
    protected ModelInterpretationContext modelInterpretationContext;

    @Override
    public ExecutionStatus configure(Context context) {

        URL url = performMultiStepModelFileSearch(true);
        if (url != null) {
            configureByResource(url);
            return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
        } else {
            return ExecutionStatus.INVOKE_NEXT_IF_ANY;
        }
    }

    private void configureByResource(URL url) {
        final String urlString = url.toString();
        if (urlString.endsWith(MODEL_CONFIG_FILE_EXTENSION)) {
            Model model = retrieveModel(url);
            if(model == null) {
                addWarn("Empty model. Abandoning.");
                return;
            }
            ModelUtil.resetForReuse(model);
            buildModelInterpretationContext(model);

            DefaultProcessor defaultProcessor = new DefaultProcessor(context, this.modelInterpretationContext);
            ModelClassToModelHandlerLinker mc2mhl = new ModelClassToModelHandlerLinker(context);
            mc2mhl.link(defaultProcessor);

            // disallow simultaneous configurations of the same context
            synchronized (context.getConfigurationLock()) {
                defaultProcessor.process(model);
            }
        } else {
            throw new LogbackException(
                    "Unexpected filename extension of file [" + url.toString() + "]. Should be " + MODEL_CONFIG_FILE_EXTENSION);
        }
    }

    private void buildModelInterpretationContext(Model topModel) {
        this.modelInterpretationContext = new ModelInterpretationContext(context, this);
        this.modelInterpretationContext.setTopModel(topModel);
        LogbackClassicDefaultNestedComponentRules.addDefaultNestedComponentRegistryRules(
                modelInterpretationContext.getDefaultNestedComponentRegistry());
        this.modelInterpretationContext.createAppenderBags();
    }

    private Model retrieveModel(URL url)  {
        long start = System.currentTimeMillis();
        try (InputStream is = url.openStream()) {
            HardenedModelInputStream hmis = new HardenedModelInputStream(is);

            Model model = (Model) hmis.readObject();
            long diff = System.currentTimeMillis() - start;
            addInfo("Model at ["+url+"] read in "+diff + " milliseconds");
            return model;
        } catch(IOException e) {
            addError("Failed to open "+url, e);
        } catch (ClassNotFoundException e) {
            addError("Failed read model object in "+ url, e);
        }
        return null;
    }

    private URL performMultiStepModelFileSearch(boolean updateState) {
        ClassLoader myClassLoader = Loader.getClassLoaderOfObject(this);
        URL url = findModelConfigFileURLFromSystemProperties(myClassLoader);
        if (url != null) {
            return url;
        }

        url = getResource(TEST_AUTOCONFIG_MODEL_FILE, myClassLoader, updateState);
        if (url != null) {
            return url;
        }

        url = getResource(AUTOCONFIG_MODEL_FILE, myClassLoader, updateState);
        return url;
    }

    URL findModelConfigFileURLFromSystemProperties(ClassLoader classLoader) {
        String logbackModelFile = OptionHelper.getSystemProperty(ClassicConstants.MODEL_CONFIG_FILE_PROPERTY);

        if (logbackModelFile != null) {
            URL result = null;
            try {
                result = new URL(logbackModelFile);
                return result;
            } catch (MalformedURLException e) {
                // so, resource is not a URL:
                // attempt to get the resource from the class path
                result = Loader.getResource(logbackModelFile, classLoader);
                if (result != null) {
                    return result;
                }
                File f = new File(logbackModelFile);
                if (f.exists() && f.isFile()) {
                    try {
                        result = f.toURI().toURL();
                        return result;
                    } catch (MalformedURLException e1) {
                    }
                }
            } finally {
                statusOnResourceSearch(logbackModelFile, result);
            }
        }
        return null;
    }


    private URL getResource(String filename, ClassLoader classLoader, boolean updateStatus) {
        URL url = Loader.getResource(filename, classLoader);
        if (updateStatus) {
            statusOnResourceSearch(filename, url);
        }
        return url;
    }

    private void statusOnResourceSearch(String resourceName, URL url) {
        StatusManager sm = context.getStatusManager();
        if (url == null) {
            sm.add(new InfoStatus("Could NOT find resource [" + resourceName + "]", context));
        } else {
            sm.add(new InfoStatus("Found resource [" + resourceName + "] at [" + url.toString() + "]", context));
        }
    }
}
