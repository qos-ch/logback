package ch.qos.logback.classic.html;

import java.util.List;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableInformation;
import ch.qos.logback.core.appender.ListAppender;

public class HTMLLayoutTest extends TestCase {

  LoggerContext lc;
  Logger logger;
  HTMLLayout layout;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    lc = new LoggerContext();
    lc.setName("default");

    ListAppender appender = new ListAppender();
    appender.setContext(lc);
    layout = new HTMLLayout();
    layout.setContext(lc);
    layout.setPattern("%level%thread%msg");
    layout.start();
    appender.setLayout(layout);
    logger = lc.getLogger(LoggerContext.ROOT_NAME);
    logger.addAppender(appender);
    appender.start();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    lc = null;
    layout = null;
  }

  @SuppressWarnings("unchecked")
  public void testHeader() throws Exception {
    String header = layout.getHeader();
    //System.out.println(header);
    
    Document doc = parseOutput(header + "</table></body></html>");
    Element rootElement = doc.getRootElement();
    Element bodyElement = rootElement.element("body");
    Element tableElement = bodyElement.element("table");
    Element trElement = tableElement.element("tr");
    List<Element> elementList = trElement.elements();
    assertEquals("Level", elementList.get(0).getText());
    assertEquals("Thread", elementList.get(1).getText());
    assertEquals("Message", elementList.get(2).getText());
  }

  public void testAppendThrowable() throws Exception {
    StringBuffer buf = new StringBuffer();
    String[] strArray = { "test1", "test2" };
    layout.throwableRenderer.render(buf, strArray);
    // System.out.println(buf.toString());
    String[] result = buf.toString().split(HTMLLayout.LINE_SEP);
    assertEquals("test1", result[0]);
    assertEquals(ThrowableRenderer.TRACE_PREFIX + "test2", result[1]);
  }

  public void testDoLayout() throws Exception {
    LoggingEvent le = createLoggingEvent();
    String result = layout.doLayout(le);
    Document doc = parseOutput(result);
    Element trElement = doc.getRootElement();
    assertEquals(3, trElement.elements().size());
    {
      Element tdElement = (Element) trElement.elements().get(0);
      assertEquals("DEBUG", tdElement.getText());
    }
    {
      Element tdElement = (Element) trElement.elements().get(1);
      assertEquals("main", tdElement.getText());
    }
    {
      Element tdElement = (Element) trElement.elements().get(2);
      assertEquals("test message", tdElement.getText());
    }
    // System.out.println(result);
  }

  @SuppressWarnings("unchecked")
  public void testDoLayoutWithException() throws Exception {
    layout.setPattern("%level %thread %msg %ex");
    LoggingEvent le = createLoggingEvent();
    le.setThrowableInformation(new ThrowableInformation(new Exception(
        "test Exception")));
    String result = layout.doLayout(le);

    String stringToParse = layout.getHeader();
    stringToParse += result;
    stringToParse += "</table></body></html>";

    System.out.println(stringToParse);
        
    Document doc = parseOutput(stringToParse);
    Element rootElement = doc.getRootElement();
    Element bodyElement = rootElement.element("body");
    Element tableElement = bodyElement.element("table");
    List<Element> trElementList = tableElement.elements();
    Element exceptionRowElement = trElementList.get(2);
    Element exceptionElement = exceptionRowElement.element("td");
    
    assertEquals(3, tableElement.elements().size());
    assertTrue(exceptionElement.getText().contains("java.lang.Exception: test Exception"));
  }
  
//  public void testLog() {
//    for (int i = 1; i <= 10; i++) {
//      if (i == 5 || i == 8) {
//        logger.debug("test", new Exception("test exception"));
//      } else {
//        logger.debug("test message" + i);
//      }
//    }
//  }

  private LoggingEvent createLoggingEvent() {
    LoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", null, null);
    return le;
  }

  Document parseOutput(String output) {
    try {
      Document document = DocumentHelper.parseText(output);
      return document;
    } catch (Exception e) {
      System.err.println(e);
      fail();
    }
    return null;
  }
}
