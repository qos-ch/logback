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
package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ceki G&uuml;lc&uuml;
 */
public class ConditionalWithoutJanino {

    LoggerContext loggerContext = new LoggerContext();
    Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

    void configure(String file) throws JoranException {
        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(loggerContext);
        jc.doConfigure(file);
    }

    // assume that janino.jar ia NOT on the classpath
    @Test
    public void conditionalWithoutJanino() throws JoranException {
        String configFile = ClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/withoutJanino.xml";
        String currentDir = System.getProperty("user.dir");
        if (!currentDir.contains("logback-classic")) {
            configFile = "logback-classic/" + configFile;
        }
        configure(configFile);
        StatusPrinter.print(loggerContext);
        StatusChecker checker = new StatusChecker(loggerContext);
        checker.assertContainsMatch(IfAction.MISSING_JANINO_MSG);

        assertSame(Level.WARN, loggerContext.getLogger("a").getLevel());
        assertSame(Level.WARN, root.getLevel());
    }

}
