/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.model.AppenderModel;
import ch.qos.logback.core.model.SiftModel;
import ch.qos.logback.core.model.processor.ModelInterpretationContext;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.testUtil.StatusChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppenderFactoryUsingSiftModelTest {

    static final String KEY = "key";

    Context context = new ContextBase();
    StatusChecker statusChecker = new StatusChecker(context);

    AppenderFactoryUsingSiftModel<Object> buildFactory() {
        AppenderModel appenderModel = new AppenderModel();
        appenderModel.setName("list-${" + KEY + "}");
        appenderModel.setClassName(ListAppender.class.getName());
        SiftModel siftModel = new SiftModel();
        siftModel.addSubModel(appenderModel);
        ModelInterpretationContext mic = new ModelInterpretationContext(context);
        return new AppenderFactoryUsingSiftModel<Object>(mic, siftModel, KEY);
    }

    @Test
    public void discriminatingValueShouldNotBeSubstituted() throws JoranException {
        context.putProperty("target", "substituted");
        String discriminatingValue = "${target}";
        Appender<Object> appender = buildFactory().buildAppender(context, discriminatingValue);
        assertNotNull(appender);
        assertEquals("list-" + discriminatingValue, appender.getName());
        statusChecker.assertIsWarningOrErrorFree();
    }

    @Test
    public void discriminatingValueWithStrayAccoladeShouldYieldAnAppender() throws JoranException {
        String discriminatingValue = "a}b";
        Appender<Object> appender = buildFactory().buildAppender(context, discriminatingValue);
        assertNotNull(appender);
        assertEquals("list-" + discriminatingValue, appender.getName());
        statusChecker.assertIsWarningOrErrorFree();
    }
}
