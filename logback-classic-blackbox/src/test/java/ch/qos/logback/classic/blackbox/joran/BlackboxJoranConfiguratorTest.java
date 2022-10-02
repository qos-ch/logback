/*
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

package ch.qos.logback.classic.blackbox.joran;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.blackbox.BlackboxClassicTestConstants;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StringListAppender;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlackboxJoranConfiguratorTest {
    LoggerContext loggerContext = new LoggerContext();
    Logger logger = loggerContext.getLogger(this.getClass().getName());
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    //StatusChecker checker = new StatusChecker(loggerContext);
    int diff = RandomUtil.getPositiveInt();

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        loggerContext.putProperty("diff", "" + diff);
        jc.doConfigure(file);

    }

    @Test
    public void eval() throws JoranException {
        configure(BlackboxClassicTestConstants.JORAN_INPUT_PREFIX + "callerData.xml");
        String msg = "hello world";
        logger.debug("toto");
        logger.debug(msg);

        StringListAppender<ILoggingEvent> slAppender = (StringListAppender<ILoggingEvent>) loggerContext
                .getLogger("root").getAppender("STR_LIST");
        assertNotNull(slAppender);
        assertEquals(2, slAppender.strList.size());
        assertTrue(slAppender.strList.get(0).contains(" DEBUG - toto"));

        String str1 = slAppender.strList.get(1);
        assertTrue(str1.contains("Caller+0"));
        assertTrue(str1.contains(" DEBUG - hello world"));
    }
}
