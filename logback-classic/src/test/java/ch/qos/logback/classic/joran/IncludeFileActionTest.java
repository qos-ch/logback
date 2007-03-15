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
import ch.qos.logback.core.util.StatusPrinter;

public class IncludeFileActionTest extends TestCase {

  LoggerContext context;
  IncludeFileAction action;

  String redirectToFile = Constants.TEST_DIR_PREFIX
      + "input/joran/redirectToFile.xml";
  String redirectToURL = Constants.TEST_DIR_PREFIX
      + "input/joran/redirectToUrl.xml";

  String urlConfig = "http://logback.qos.ch/simpleConfig.xml";
  String simpleConfig = Constants.TEST_DIR_PREFIX
      + "input/joran/simpleConfig.xml";
  String invalidConfig = Constants.TEST_DIR_PREFIX
      + "input/joran/invalidConfig.xml";

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
    System.setProperty("testing.value.file", simpleConfig);
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToFile);

    verifyConfig();
  }

  public void testNoFileFound() throws JoranException {
    System.setProperty("testing.value.file", "toto");
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToFile);

    assertEquals(2, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
  }

  public void testWithCorruptFile() throws JoranException {
    System.setProperty("testing.value.file", invalidConfig);
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToFile);

    assertEquals(10, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
  }

//  public void testURLOK() throws JoranException {
//    //This one needs that we put a file on the web
//    //and requires a net connection on the test-runner's side.
//    System.setProperty("testing.value.url", urlConfig);
//    JoranConfigurator jc = new JoranConfigurator();
//    jc.setContext(context);
//    jc.doConfigure(redirectToURL);
//
//    verifyConfig();
//  }

  public void testMalformedURL() throws JoranException {
    System.setProperty("testing.value.url", "htp://logback.qos.ch");
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToURL);

    assertEquals(2, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
  }

  public void testUnknownURL() throws JoranException {
    System.setProperty("testing.value.url", "http://logback2345.qos.ch");
    JoranConfigurator jc = new JoranConfigurator();
    jc.setContext(context);
    jc.doConfigure(redirectToURL);

    assertEquals(2, context.getStatusManager().getCount());
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
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
