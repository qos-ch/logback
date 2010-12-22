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
    assertEquals(0, tracker.bufferCount);
  }

  @Test
  public void empty1() {
    long now = 3000;
    assertNotNull(tracker.getOrCreate(key, now++));
    now += CyclicBufferTracker.THRESHOLD + 1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);

    assertNotNull(tracker.getOrCreate(key, now++));
  }

  @Test
  public void smoke() {
    long now = 3000;
    CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
    assertEquals(cb, tracker.getOrCreate(key, now++));
    now += AppenderTrackerImpl.THRESHOLD + 1000;
    tracker.clearStaleBuffers(now);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);
  }

  @Test
  public void destroy() {
    long now = 3000;
    CyclicBuffer<Object> cb = tracker.getOrCreate(key, now);
    cb.add(new Object());
    assertEquals(1, cb.length());
    tracker.removeBuffer(key);
    assertEquals(0, tracker.keyList().size());
    assertEquals(0, tracker.bufferCount);
    assertEquals(0, cb.length());
  }




}
