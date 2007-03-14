package ch.qos.logback.classic.joran;

import junit.framework.TestCase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.joran.action.IncludeFileAction;
import ch.qos.logback.classic.util.Constants;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;

public class IncludeFileActionTest extends TestCase {

  LoggerContext context;
  IncludeFileAction action;

  String filePath = Constants.TEST_DIR_PREFIX
      + "input/joran/redirectConfig.xml";
  String invalidRedirect = Constants.TEST_DIR_PREFIX
      + "input/joran/invalidRedirect.xml";
  String filePathWithSubst = Constants.TEST_DIR_PREFIX
      + "input/joran/redirectWithSubst.xml";
  String redirectToInvalid = Constants.TEST_DIR_PREFIX
      + "input/joran/redirectToInvalid.xml";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = new LoggerContext();
    action = new IncludeFileAction();
    action.setContext(context);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    action = null;
    context = null;
  }

  public void testLoadFileOK() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(filePath);

    verifyConfig();
  }

  public void testNoFileFound() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(invalidRedirect);

    assertEquals(3, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
  }

  public void testWithCorruptFile() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToInvalid);

    assertEquals(10, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
  }

  public void testWithSubst() throws JoranException {
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(filePathWithSubst);

    verifyConfig();
  }

  private void verifyConfig() {
    Logger logger = context.getLogger(LoggerContext.ROOT_NAME);
    Appender appender = (ConsoleAppender) logger.getAppender("redirectConsole");
    assertNotNull(appender);
    PatternLayout layout = (PatternLayout) appender.getLayout();
    assertNotNull(layout);
    assertEquals("%d - %m%n", layout.getPattern());
  }
}
