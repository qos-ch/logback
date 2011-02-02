package ch.qos.logback.classic.boolex;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.conditional.IfAction;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ceki G&uuml;c&uuml;
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
  public void condtionalWithoutJanino() throws JoranException {
    configure(ClassicTestConstants.JORAN_INPUT_PREFIX + "conditional/withoutJanino.xml");
    StatusPrinter.print(loggerContext);
    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.containsMatch(IfAction.MISSING_JANINO_MSG));
    assertSame(Level.WARN, root.getLevel());
  }

}

