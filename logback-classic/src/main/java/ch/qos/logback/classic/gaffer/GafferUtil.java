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
package ch.qos.logback.classic.gaffer;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class GafferUtil {

    private static String ERROR_MSG = "Failed to instantiate " + ClassicConstants.GAFFER_CONFIGURATOR_FQCN;

    public static void runGafferConfiguratorOn(LoggerContext loggerContext, Object origin, File configFile) {
        GafferConfigurator gafferConfigurator = GafferUtil.newGafferConfiguratorInstance(loggerContext, origin);
        if (gafferConfigurator != null) {
            gafferConfigurator.run(configFile);
        }
    }

    public static void runGafferConfiguratorOn(LoggerContext loggerContext, Object origin, URL configFile) {
        GafferConfigurator gafferConfigurator = GafferUtil.newGafferConfiguratorInstance(loggerContext, origin);
        if (gafferConfigurator != null) {
            gafferConfigurator.run(configFile);
        }
    }

    private static GafferConfigurator newGafferConfiguratorInstance(LoggerContext loggerContext, Object origin) {

        try {
            Class gcClass = Class.forName(ClassicConstants.GAFFER_CONFIGURATOR_FQCN);
            Constructor c = gcClass.getConstructor(LoggerContext.class);
            return (GafferConfigurator) c.newInstance(loggerContext);
        } catch (ClassNotFoundException e) {
            addError(loggerContext, origin, ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            addError(loggerContext, origin, ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            addError(loggerContext, origin, ERROR_MSG, e);
        } catch (InstantiationException e) {
            addError(loggerContext, origin, ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            addError(loggerContext, origin, ERROR_MSG, e);
        }
        return null;
    }

    private static void addError(LoggerContext context, Object origin, String msg) {
        addError(context, origin, msg, null);
    }

    private static void addError(LoggerContext context, Object origin, String msg, Throwable t) {
        StatusManager sm = context.getStatusManager();
        if (sm == null) {
            return;
        }
        sm.add(new ErrorStatus(msg, origin, t));
    }

}
