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
package ch.qos.logback.core.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.hook.DefaultShutdownHook;
import ch.qos.logback.core.hook.ShutdownHookBase;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Action which handles &lt;shutdownHook&gt; elements in configuration files.
 * 
 * @author Mike Reinhold
 */
public class ShutdownHookAction extends Action {

    ShutdownHookBase hook;
    private boolean inError;

    /**
     * Instantiates a shutdown hook of the given class and sets its name.
     * 
     * The hook thus generated is placed in the {@link InterpretationContext}'s
     * shutdown hook bag.
     */
    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        hook = null;
        inError = false;

        String className = attributes.getValue(CLASS_ATTRIBUTE);
        if (OptionHelper.isEmpty(className)) {
            className = DefaultShutdownHook.class.getName();
            addInfo("Assuming className [" + className + "]");
        }

        try {
            addInfo("About to instantiate shutdown hook of type [" + className + "]");

            hook = (ShutdownHookBase) OptionHelper.instantiateByClassName(className, ShutdownHookBase.class, context);
            hook.setContext(context);

            ic.pushObject(hook);
        } catch (Exception e) {
            inError = true;
            addError("Could not create a shutdown hook of type [" + className + "].", e);
            throw new ActionException(e);
        }
    }

    /**
     * Once the children elements are also parsed, now is the time to activate the
     * shutdown hook options.
     */
    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        if (inError) {
            return;
        }

        Object o = ic.peekObject();
        if (o != hook) {
            addWarn("The object at the of the stack is not the hook pushed earlier.");
        } else {
            ic.popObject();

            Thread hookThread = new Thread(hook, "Logback shutdown hook [" + context.getName() + "]");
            addInfo("Registeting shuthown hook with JVM runtime.");
            context.putObject(CoreConstants.SHUTDOWN_HOOK_THREAD, hookThread);
            Runtime.getRuntime().addShutdownHook(hookThread);
        }
    }
}
