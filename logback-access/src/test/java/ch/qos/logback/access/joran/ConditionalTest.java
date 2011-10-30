package ch.qos.logback.access.joran;

import ch.qos.logback.access.TeztConstants;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ConditionalTest {

  AccessContext context = new AccessContext();
  StatusChecker checker = new StatusChecker(context);

  int diff = RandomUtil.getPositiveInt();
  String randomOutputDir = CoreTestConstants.OUTPUT_DIR_PREFIX + diff + "/";

  @Before
  public void setUp() {
    InetAddress localhost = null;
    try {
      localhost = InetAddress.getLocalHost();
      context.putProperty("aHost", localhost.getHostName());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  void configure(String file) throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(file);
  }

  @Test
  public void conditionalConsoleApp_IF_THEN_True() throws JoranException, UnknownHostException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole.xml");
    ConsoleAppender consoleAppender = (ConsoleAppender) context.getAppender("CON");
    assertNotNull(consoleAppender);
    assertTrue(checker.isErrorFree(0));
  }

  @Test
  public void conditionalConsoleApp_IF_THEN_False() throws JoranException,
          IOException, InterruptedException {
    context.putProperty("aHost", null);
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole.xml");

    ConsoleAppender consoleAppender = (ConsoleAppender) context.getAppender("CON");
    assertNull(consoleAppender);

    StatusChecker checker = new StatusChecker(context);
    assertTrue(checker.isErrorFree(0));
  }

  @Test
  public void conditionalConsoleApp_ELSE() throws JoranException,
          IOException, InterruptedException {
    configure(TeztConstants.TEST_DIR_PREFIX + "input/joran/conditional/conditionalConsole_ELSE.xml");
    ConsoleAppender consoleAppender = (ConsoleAppender) context.getAppender("CON");
    assertNull(consoleAppender);

    ListAppender listAppender = (ListAppender) context.getAppender("LIST");
    assertNotNull(listAppender);
    assertTrue(checker.isErrorFree(0));
  }
}
