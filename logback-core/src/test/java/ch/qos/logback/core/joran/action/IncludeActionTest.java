package ch.qos.logback.core.joran.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.TrivialConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class IncludeActionTest  {

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
  static final String URL_TO_INCLUDE = "file:./" + INCLUDED_FILE;

  static final String INVALID = INCLUSION_DIR_PREFIX + "invalid.xml";

  static final String INCLUDED_AS_RESOURCE = "input/joran/inclusion/includedAsResource.xml";

  public IncludeActionTest() {
    HashMap<Pattern, Action> rulesMap = new HashMap<Pattern, Action>();
    rulesMap.put(new Pattern("x"), new NOPAction());
    rulesMap.put(new Pattern("x/inc"), new IncAction());
    rulesMap.put(new Pattern("x/include"), new IncludeAction());

    tc = new TrivialConfigurator(rulesMap);
    tc.setContext(context);
  }

  @Before
  public void setUp() throws Exception {
    IncAction.beginCount = 0;
    IncAction.errorCount = 0;
    IncAction.endCount = 0;
  }

  @After
  public void tearDown() throws Exception {
    context = null;
    System.clearProperty(FILE_KEY);
  }

  @Test
  public void basicFile() throws JoranException {
    System.setProperty(FILE_KEY, INCLUDED_FILE);
    tc.doConfigure(INCLUDE_BY_FILE);
    verifyConfig(2);
  }

  @Test
  public void basicResource() throws JoranException {
    System.setProperty(FILE_KEY, INCLUDED_AS_RESOURCE);
    tc.doConfigure(INCLUDE_BY_RESOURCE);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test
   public void testBasicURL() throws JoranException {
    System.setProperty(FILE_KEY, URL_TO_INCLUDE);
    tc.doConfigure(INCLUDE_BY_URL);
    StatusPrinter.print(context);
    verifyConfig(2);
  }

  @Test
  public void noFileFound() throws JoranException {
    System.setProperty(FILE_KEY, "toto");
    tc.doConfigure(INCLUDE_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(FileNotFoundException.class));
  }

  @Test
  public void withCorruptFile() throws JoranException {
    System.setProperty(FILE_KEY, INVALID);
    tc.doConfigure(INCLUDE_BY_FILE);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(SAXParseException.class));
  }

  @Test
  public void malformedURL() throws JoranException {
    System.setProperty(FILE_KEY, "htp://logback.qos.ch");
    tc.doConfigure(INCLUDE_BY_URL);
    assertEquals(Status.ERROR, context.getStatusManager().getLevel());
    StatusChecker sc = new StatusChecker(context.getStatusManager());
    assertTrue(sc.containsException(MalformedURLException.class));
  }

  @Test
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
