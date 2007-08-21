package ch.qos.logback.core.joran.action;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.xml.sax.SAXParseException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class IncludeFileActionTest extends TestCase {

  final static String FILE_KEY = "testing";

  Context context = new ContextBase();
  TrivialConfigurator tc;

  static final String INCLUSION_DIR_PREFIX = "src/test/input/joran/inclusion/";

  static final String INCLUDE_BY_FILE = INCLUSION_DIR_PREFIX
      + "includeByFile.xml";
  static final String INCLUDE_BY_URL = INCLUSION_DIR_PREFIX
      + "includeByUrl.xml";

  static final String INCLUDE_BY_RESOURCE = INCLUSION_DIR_PREFIX
      + "includeByResource.xml";

  static final String INCLUDED_FILE = INCLUSION_DIR_PREFIX + "included.xml";
  static final String URL_TO_INCLUDE = "file://./" + INCLUDED_FILE;

  static final String INVALID = INCLUSION_DIR_PREFIX + "invalid.xml";

  static final String INCLUDED_AS_RESOURCE = "input/joran/inclusion/includedAsResource.xml";

  public IncludeFileActionTest(String arg0) {
    super(arg0);
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x"), new NOPAction());
    rulesMap.put(new Pattern("x/inc"), new IncAction());
    rulesMap.put(new Pattern("x/include"), new IncludeFileAction());

    tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    IncAction.beginCount = 0;
    IncAction.errorCount = 0;
    IncAction.endCount = 0;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
    System.clearProperty(FILE_KEY);
  }

  public void testBasicFile() throws JoranException {
    System.setProperty(FILE_KEY, INCLUDED_FILE);
    tc.doConfigure(INCLUDE_BY_FILE);
    verifyConfig(2);
  }

  public void testBasicResource() throws JoranException {
    System.setProperty(FILE_KEY, INCLUDED_AS_RESOURCE);
    tc.doConfigure(INCLUDE_BY_RESOURCE);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  // public void testBasicURL() throws JoranException {
  // System.setProperty(FILE_KEY, URL_TO_INCLUDE);
  // tc.doConfigure(INCLUDE_BY_URL);
  // StatusPrinter.print(context);
  // verifyConfig(2);
  // }

  public void testNoFileFound() throws JoranException {
    System.setProperty(FILE_KEY, "toto");
    tc.doConfigure(INCLUDE_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(FileNotFoundException.class));
  }

  public void testWithCorruptFile() throws JoranException {
    System.setProperty(FILE_KEY, INVALID);
    tc.doConfigure(INCLUDE_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(SAXParseException.class));
  }

  public void testMalformedURL() throws JoranException {
    System.setProperty(FILE_KEY, "htp://logback.qos.ch");
    tc.doConfigure(INCLUDE_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(MalformedURLException.class));
  }

  public void testUnknownURL() throws JoranException {
    System.setProperty(FILE_KEY, "http://logback2345.qos.ch");
    tc.doConfigure(INCLUDE_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(UnknownHostException.class));
  }

  void verifyConfig(int expected) {
    assertEquals(expected, IncAction.beginCount);
    assertEquals(expected, IncAction.endCount);
  }
}
