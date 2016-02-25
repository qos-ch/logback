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
package ch.qos.logback.classic.joran.action;

import org.xml.sax.Attributes;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionConst;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Action which handles <logger> elements in configuration files.
 * 
 * @author Ceki Gulcu
 */
public class LoggerAction extends Action {
    public static final String LEVEL_ATTRIBUTE = "level";

    boolean inError = false;
    Logger logger;

    public void begin(InterpretationContext ec, String name, Attributes attributes) {
        // Let us forget about previous errors (in this object)
        inError = false;
        logger = null;

        LoggerContext loggerContext = (LoggerContext) this.context;

        String loggerName = ec.subst(attributes.getValue(NAME_ATTRIBUTE));

        if (OptionHelper.isEmpty(loggerName)) {
            inError = true;
            String aroundLine = getLineColStr(ec);
            String errorMsg = "No 'name' attribute in element " + name + ", around " + aroundLine;
            addError(errorMsg);
            return;
        }

        logger = loggerContext.getLogger(loggerName);

        String levelStr = ec.subst(attributes.getValue(LEVEL_ATTRIBUTE));

        if (!OptionHelper.isEmpty(levelStr)) {
            if (ActionConst.INHERITED.equalsIgnoreCase(levelStr) || ActionConst.NULL.equalsIgnoreCase(levelStr)) {
                addInfo("Setting level of logger [" + loggerName + "] to null, i.e. INHERITED");
                logger.setLevel(null);
            } else {
                Level level = Level.toLevel(levelStr);
                addInfo("Setting level of logger [" + loggerName + "] to " + level);
                logger.setLevel(level);
            }
        }

        String additivityStr = ec.subst(attributes.getValue(ActionConst.ADDITIVITY_ATTRIBUTE));
        if (!OptionHelper.isEmpty(additivityStr)) {
            boolean additive = OptionHelper.toBoolean(additivityStr, true);
            addInfo("Setting additivity of logger [" + loggerName + "] to " + additive);
            logger.setAdditive(additive);
        }
        ec.pushObject(logger);
    }

    public void end(InterpretationContext ec, String e) {
        if (inError) {
            return;
        }
        Object o = ec.peekObject();
        if (o != logger) {
            addWarn("The object on the top the of the stack is not " + logger + " pushed earlier");
            addWarn("It is: " + o);
        } else {
            ec.popObject();
        }
    }

    public void finish(InterpretationContext ec) {
    }
}
