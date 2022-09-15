/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.model.processor;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.model.Model;
import ch.qos.logback.core.model.ShutdownHookModel;
import ch.qos.logback.core.util.DynamicClassLoadingException;
import ch.qos.logback.core.util.IncompatibleClassException;
import ch.qos.logback.core.util.OptionHelper;

public class ShutdownHookModelHandler extends ModelHandlerBase {

    static final String OLD_SHUTDOWN_HOOK_CLASSNAME = "ch.qos.logback.core.hook.DelayingShutdownHook";
    static final String DEFAULT_SHUTDOWN_HOOK_CLASSNAME = DefaultShutdownHook.class.getName();
    static public final String RENAME_WARNING = OLD_SHUTDOWN_HOOK_CLASSNAME + " was renamed as "+ DEFAULT_SHUTDOWN_HOOK_CLASSNAME;

    public ShutdownHookModelHandler(Context context) {
        super(context);
    }
    boolean inError = false;
    ShutdownHook  hook = null;

    static public ModelHandlerBase makeInstance(Context context, ModelInterpretationContext mic) {
        return new ShutdownHookModelHandler(context);
    }

    @Override
    protected Class<ShutdownHookModel> getSupportedModelClass() {
        return ShutdownHookModel.class;
    }

    @Override
    public void handle(ModelInterpretationContext mic, Model model) {

        ShutdownHookModel shutdownHookModel = (ShutdownHookModel) model;

        String className = shutdownHookModel.getClassName();
        if (OptionHelper.isNullOrEmpty(className)) {
            className = DEFAULT_SHUTDOWN_HOOK_CLASSNAME;
            addInfo("Assuming className [" + className + "]");
        } else {
            className = mic.getImport(className);
            if(className.equals(OLD_SHUTDOWN_HOOK_CLASSNAME)) {
                className = DEFAULT_SHUTDOWN_HOOK_CLASSNAME;
                addWarn(RENAME_WARNING);
                addWarn("Please use the new class name");
            }
        }

        addInfo("About to instantiate shutdown hook of type [" + className + "]");

        try {
            hook = (ShutdownHookBase) OptionHelper.instantiateByClassName(className, ShutdownHookBase.class, context);
            hook.setContext(context);
        } catch (IncompatibleClassException | DynamicClassLoadingException e) {
            addError("Could not create a shutdown hook of type [" + className + "].", e);
            inError = true;
            return;
        }

        mic.pushObject(hook);
    }

    @Override
    public void postHandle(ModelInterpretationContext mic, Model model) throws ModelHandlerException {
        if (inError) {
            return;
        }
        Object o = mic.peekObject();

        if (o != hook) {
            addWarn("The object on the top the of the stack is not the hook object pushed earlier.");
        } else {
            Thread hookThread = new Thread(hook, "Logback shutdown hook [" + context.getName() + "]");
            addInfo("Registering shutdown hook with JVM runtime.");
            context.putObject(CoreConstants.SHUTDOWN_HOOK_THREAD, hookThread);
            Runtime.getRuntime().addShutdownHook(hookThread);

            mic.popObject();
        }
    }
}