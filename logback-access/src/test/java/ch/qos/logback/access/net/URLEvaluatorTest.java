package ch.qos.logback.access.net;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EvaluationException;

public class URLEvaluatorTest  {

  final String expectedURL1 = "testUrl1";
  final String expectedURL2 = "testUrl2";
  Context context = new ContextBase();
  URLEvaluator evaluator;    
  DummyRequest request;
  DummyResponse response;
  DummyServerAdapter serverAdapter;
  
  @Before
  public void setUp() throws Exception {
    evaluator = new URLEvaluator();
    evaluator.setContext(context);
    evaluator.addURL(expectedURL1);
    evaluator.start();
    request = new DummyRequest();
    response = new DummyResponse();
    serverAdapter = new DummyServerAdapter(request, response);
  }
  
  @After
  public void tearDown() throws Exception {
    evaluator.stop();
    evaluator = null;
    request = null;
    response = null;
    serverAdapter = null;
    context = null;
  }
  
  @Test
  public void testExpectFalse() throws EvaluationException {
    request.setRequestUri("test");
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    assertFalse(evaluator.evaluate(ae));
  }
  
  @Test
  public void testExpectTrue() throws EvaluationException {
    request.setRequestUri(expectedURL1);   
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    assertTrue(evaluator.evaluate(ae));    
  }
  
  @Test
  public void testExpectTrueMultiple() throws EvaluationException {
    evaluator.addURL(expectedURL2);
    request.setRequestUri(expectedURL2);    
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    assertTrue(evaluator.evaluate(ae));    
  }
}
