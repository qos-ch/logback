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

import static ch.qos.logback.core.CoreConstants.FA_FILENAME_COLLISION_MAP;
import static ch.qos.logback.core.CoreConstants.RFA_FILENAME_PATTERN_COLLISION_MAP;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.rolling.helper.FileNamePattern;
import ch.qos.logback.core.spi.ContextAwareBase;

public class ContextUtil extends ContextAwareBase {

    static final String GROOVY_RUNTIME_PACKAGE = "org.codehaus.groovy.runtime";
    //static final String SYSTEM_LOGGER_FQCN = "java.lang.System$Logger";

    public ContextUtil(final Context context) {
        setContext(context);
    }

    public void addProperties(final Properties props) {
        if (props == null) {
            return;
        }

        for(final Entry<Object, Object> e: props.entrySet()) {
            final String key = (String) e.getKey();
            context.putProperty(key, (String) e.getValue());
        }

    }

    public static Map<String, String> getFilenameCollisionMap(final Context context) {
        if (context == null) {
            return null;
        }
        return (Map<String, String>) context.getObject(FA_FILENAME_COLLISION_MAP);
    }

    public static Map<String, FileNamePattern> getFilenamePatternCollisionMap(final Context context) {
        if (context == null) {
            return null;
        }
        return (Map<String, FileNamePattern>) context
                        .getObject(RFA_FILENAME_PATTERN_COLLISION_MAP);
    }

    public void addGroovyPackages(final List<String> frameworkPackages) {
        addFrameworkPackage(frameworkPackages, GROOVY_RUNTIME_PACKAGE);
    }

    public void addFrameworkPackage(final List<String> frameworkPackages, final String packageName) {
        if (!frameworkPackages.contains(packageName)) {
            frameworkPackages.add(packageName);
        }
    }

}
