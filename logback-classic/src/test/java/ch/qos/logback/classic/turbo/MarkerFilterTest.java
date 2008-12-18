package ch.qos.logback.classic.turbo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.core.spi.FilterReply;

public class MarkerFilterTest {

  static String MARKER_NAME = "toto";
  
  Marker totoMarker = MarkerFactory.getMarker(MARKER_NAME);
  

  @Test
  public void testNoMarker() {
    MarkerFilter mkt = new MarkerFilter();
    mkt.start();
    assertFalse(mkt.isStarted());
    assertEquals(FilterReply.NEUTRAL, mkt.decide(totoMarker, null, null, null, null, null));
    assertEquals(FilterReply.NEUTRAL, mkt.decide(null, null, null, null, null, null));

  }


  @Test
  public void testBasic() {
    MarkerFilter mkt = new MarkerFilter();
    mkt.setMarker(MARKER_NAME);
    mkt.setOnMatch("ACCEPT");
    mkt.setOnMismatch("DENY");

    mkt.start();
    assertTrue(mkt.isStarted());
    assertEquals(FilterReply.DENY, mkt.decide(null, null, null, null, null, null));
    assertEquals(FilterReply.ACCEPT, mkt.decide(totoMarker, null, null, null, null, null));
  }
  
}
