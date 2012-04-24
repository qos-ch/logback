package ch.qos.logback.classic.issue;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Before;
import org.junit.Test;

public class LBCORE_254 {

  static String ISSUES_PREFIX =   ClassicTestConstants.JORAN_INPUT_PREFIX+"issues/";
  LoggerContext context = new LoggerContext();

  StatusChecker checker = new StatusChecker(context);

  @Before
  public void setUp() {
    context.start();
  }

  @Test
  public void sysProps() throws JoranException {
    System.setProperty("k.lbcore254", ISSUES_PREFIX+"lbcore254");
    JoranConfigurator configurator = new JoranConfigurator();
    configurator.setContext(context);
    configurator.doConfigure(ISSUES_PREFIX+"lbcore254.xml");

    checker.isErrorFree(0);

  }
}
