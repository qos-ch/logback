package ch.qos.logback.access.net;

import junit.framework.TestCase;
import ch.qos.logback.access.pattern.helpers.DummyRequest;
import ch.qos.logback.access.pattern.helpers.DummyResponse;
import ch.qos.logback.access.pattern.helpers.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EvaluationException;

public class URLEvaluatorTest extends TestCase {

  final String expectedURL1 = "testUrl1";
  final String expectedURL2 = "testUrl2";
  Context context = new ContextBase();
  URLEvaluator evaluator;    
  DummyRequest request;
  DummyResponse response;
  DummyServerAdapter serverAdapter;
  
  public void setUp() throws Exception {
    evaluator = new URLEvaluator();
    evaluator.setContext(context);
    evaluator.addURL(expectedURL1);
    evaluator.start();
    request = new DummyRequest();
    response = new DummyResponse();
    serverAdapter = new DummyServerAdapter(request, response);
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    evaluator.stop();
    evaluator = null;
    request = null;
    response = null;
    serverAdapter = null;
    context = null;
  }
  
  public void testExpectFalse() throws EvaluationException {
    request.setRequestUri("test");
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    System.out.println(ae.getRequestURL());
    assertFalse(evaluator.evaluate(ae));
  }
  
  public void testExpectTrue() throws EvaluationException {
    request.setRequestUri(expectedURL1);   
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    System.out.println(ae.getRequestURL());
    assertTrue(evaluator.evaluate(ae));    
  }
  
  public void testExpectTrueMultiple() throws EvaluationException {
    evaluator.addURL(expectedURL2);
    request.setRequestUri(expectedURL2);    
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    System.out.println(ae.getRequestURL());
    assertTrue(evaluator.evaluate(ae));    
  }
}
