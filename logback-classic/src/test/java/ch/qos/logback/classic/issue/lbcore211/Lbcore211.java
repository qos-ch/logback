package ch.qos.logback.classic.issue.lbcore211;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class Lbcore211 {

  @Test
  public void lbcore211() throws JoranException {

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(lc);
    lc.reset();
    configurator.doConfigure("/home/ceki/lbcore211.xml");

    Logger l = lc.getLogger("file.logger");
    StatusPrinter.print(lc);
    for (int i = 0; i < 10; i++) {
      l.info("hello " + i);
    }

    lc.stop();
  }
}
