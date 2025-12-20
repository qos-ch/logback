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
package ch.qos.logback.core.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.ShutdownHook;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ContextUtil extends ContextAwareBase {

    static final String GROOVY_RUNTIME_PACKAGE = "org.codehaus.groovy.runtime";
    // static final String SYSTEM_LOGGER_FQCN = "java.lang.System$Logger";

    public ContextUtil(Context context) {
        setContext(context);
    }

    public void addProperties(Properties props) {
        if (props == null) {
            return;
        }

        for (Entry<Object, Object> e : props.entrySet()) {
            String key = (String) e.getKey();
            context.putProperty(key, (String) e.getValue());
        }

    }

    public void addGroovyPackages(List<String> frameworkPackages) {
        addFrameworkPackage(frameworkPackages, GROOVY_RUNTIME_PACKAGE);
    }

    public void addFrameworkPackage(List<String> frameworkPackages, String packageName) {
        if (!frameworkPackages.contains(packageName)) {
            frameworkPackages.add(packageName);
        }
    }

    /**
     * Add a shutdown hook thread with the JVM runtime.
     *
     * If a previous shutdown hook thread was registered, it is replaced.
     * @param hook
     * @since 1.5.7
     */
    public void addOrReplaceShutdownHook(ShutdownHook hook) {
        Runtime runtime = Runtime.getRuntime();

        Thread oldShutdownHookThread = (Thread) context.getObject(CoreConstants.SHUTDOWN_HOOK_THREAD);
        if(oldShutdownHookThread != null) {
            addInfo("Removing old shutdown hook from JVM runtime");
            runtime.removeShutdownHook(oldShutdownHookThread);
        }

        Thread hookThread = new Thread(hook, "Logback shutdown hook [" + context.getName() + "]");
        addInfo("Registering shutdown hook with JVM runtime.");
        context.putObject(CoreConstants.SHUTDOWN_HOOK_THREAD, hookThread);
        runtime.addShutdownHook(hookThread);

    }

}
