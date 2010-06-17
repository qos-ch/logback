package ch.qos.logback.core.spi;

import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.sift.AppenderTrackerImpl;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class CyclicBufferTrackerImplTest {


  CyclicBufferTrackerImpl<Object> tracker = new CyclicBufferTrackerImpl<Object>();
  String key = "a";

  @Test
  public void empty0() {
    long now = 3000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
  }

    @Test
  public void empty1() {
    long now = 3000;
    assertNotNull(tracker.get(key, now++));
    now += CyclicBufferTracker.THRESHOLD+1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertNotNull(tracker.get(key, now++));
  }

  @Test
  public void smoke() {
    long now = 3000;
    CyclicBuffer<Object> cb = tracker.get(key, now);
    assertEquals(cb, tracker.get(key, now++));
    now += AppenderTrackerImpl.THRESHOLD+1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
  }
}
