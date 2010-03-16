package ch.qos.logback.classic.encoder;

import static ch.qos.logback.core.CoreConstants.CODES_URL;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class LayoutInsteadOfEncoderTest {

  // TeztConstants.TEST_DIR_PREFIX + "input/joran/ignore.xml"
  JoranConfigurator jc = new JoranConfigurator();
  LoggerContext loggerContext = new LoggerContext();

  @Before
  public void setUp() {
    jc.setContext(loggerContext);

  }

  // jc.doConfigure(TeztConstants.TEST_DIR_PREFIX + "input/joran/ignore.xml");

  @Test
  public void layoutInsteadOfEncoer() throws JoranException {
    jc.doConfigure(ClassicTestConstants.JORAN_INPUT_PREFIX
        + "compatibility/layoutInsteadOfEncoder.xml");
    StatusPrinter.print(loggerContext);
    StatusChecker checker = new StatusChecker(loggerContext);
    assertTrue(checker.containsMatch(Status.ERROR, "This appender no longer admits a layout as a sub-component"));
    assertTrue(checker.containsMatch(Status.ERROR, "See also "+CODES_URL+"#layoutInsteadOfEncoder for details"));
  }
}
