package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.core.spi.FilterReply;

import junit.framework.TestCase;

public class MarkerTurboFilterTest extends TestCase {

  static String MARKER_NAME = "toto";
  
  Marker totoMarker = MarkerFactory.getMarker(MARKER_NAME);
  
  public MarkerTurboFilterTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNoMarker() {
    MarkerTurboFilter mkt = new MarkerTurboFilter();
    mkt.start();
    assertFalse(mkt.isStarted());
    assertEquals(FilterReply.NEUTRAL, mkt.decide(totoMarker, null, null, null, null, null));
    assertEquals(FilterReply.NEUTRAL, mkt.decide(null, null, null, null, null, null));

  }
  
  public void testBasic() {
    MarkerTurboFilter mkt = new MarkerTurboFilter();
    mkt.setMarker(totoMarker);
    mkt.setOnMatch("ACCEPT");
    mkt.setOnMismatch("DENY");

    mkt.start();
    assertTrue(mkt.isStarted());
    assertEquals(FilterReply.DENY, mkt.decide(null, null, null, null, null, null));
    assertEquals(FilterReply.ACCEPT, mkt.decide(totoMarker, null, null, null, null, null));
  }
  
}
