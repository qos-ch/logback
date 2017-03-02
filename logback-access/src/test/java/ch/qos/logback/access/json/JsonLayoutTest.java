package ch.qos.logback.access.json;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.access.dummy.DummyRequest;
import ch.qos.logback.access.dummy.DummyResponse;
import ch.qos.logback.access.dummy.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;

/**
 * @author Pierre Queinnec
 */
public class JsonLayoutTest {

  AccessContext context;
  JsonLayout layout;

  @Before
  public void setUp() throws Exception {
    context = new AccessContext();
    context.setName("default");

    layout = new JsonLayout();
    layout.setContext(context);
    layout.start();
  }

  @After
  public void tearDown() throws Exception {
    context = null;
    layout = null;
  }

  @Test
  public void testEventWithDefaultConfig() {
    String correctResultRegex = "\\{\"method\":\"testMethod\","
        + "\"protocol\":\"testProtocol\","
        + "\"remote-addr\":\"testRemoteAddress\","
        + "\"remote-host\":\"testHost\"," + "\"remote-user\":\"testUser\","
        + "\"request-uri\":\"http://localhost:8080/test/index.html\","
        + "\"server-name\":\"testServerName\"," + "\"status-code\":200,"
        + "\"timestamp\":\\d+\\}";

    String result = layout.doLayout(createAccessEvent());
    System.out.println(result);

    assertTrue(result.matches(correctResultRegex));
  }

  @Test
  public void testEventWithFullConfig() {
    layout.setIncludeContentLength(true);
    layout.setIncludeLocalPort(true);
    layout.setIncludeRequestURL(true);

    String correctResultRegex = "\\{\"content-length\":1000,"
        + "\"local-port\":11,"
        + "\"method\":\"testMethod\","
        + "\"protocol\":\"testProtocol\","
        + "\"remote-addr\":\"testRemoteAddress\","
        + "\"remote-host\":\"testHost\","
        + "\"remote-user\":\"testUser\","
        + "\"request-uri\":\"http://localhost:8080/test/index.html\","
        + "\"request-url\":\"testMethod http://localhost:8080/test/index.html testProtocol\","
        + "\"server-name\":\"testServerName\"," + "\"status-code\":200,"
        + "\"timestamp\":\\d+\\}";

    String result = layout.doLayout(createAccessEvent());
    System.out.println(result);

    assertTrue(result.matches(correctResultRegex));
  }

  private IAccessEvent createAccessEvent() {
    DummyRequest request = new DummyRequest();
    request.setRequestUri("http://localhost:8080/test/index.html");
    DummyResponse response = new DummyResponse();
    DummyServerAdapter adapter = new DummyServerAdapter(request, response);

    return new AccessEvent(request, response, adapter);
  }

}
