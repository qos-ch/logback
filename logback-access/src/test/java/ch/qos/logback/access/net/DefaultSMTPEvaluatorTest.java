package ch.qos.logback.access.net;

import junit.framework.TestCase;
import ch.qos.logback.access.pattern.helpers.DummyRequest;
import ch.qos.logback.access.pattern.helpers.DummyResponse;
import ch.qos.logback.access.pattern.helpers.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.boolex.EvaluationException;

public class DefaultSMTPEvaluatorTest extends TestCase {

  final String expectedURL = "testUrl";
  Context context = new ContextBase();
  DefaultSMTPEvaluator evaluator;    
  DummyRequest request;
  DummyResponse response;
  DummyServerAdapter serverAdapter;
  
  public void setUp() throws Exception {
    evaluator = new DefaultSMTPEvaluator();
    evaluator.setContext(context);
    evaluator.setUrl(expectedURL);
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
  
  public void testExpectFalseBecauseOfStatus() throws EvaluationException {
    request.setRequestUrl("test");
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    assertFalse(evaluator.evaluate(ae));
  }
  
  public void testExpectTrue() throws EvaluationException {
    request.setRequestUrl(expectedURL);    
    AccessEvent ae = new AccessEvent(request, response, serverAdapter);
    assertTrue(evaluator.evaluate(ae));    
  }
}
