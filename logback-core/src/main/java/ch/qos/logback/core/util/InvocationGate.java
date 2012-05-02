package ch.qos.logback.core.util;

/**
 * This class serves as a gateway for invocations of a "costly" operation on a critical execution path.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class InvocationGate {

  // experiments indicate that even for the most CPU intensive applications with 200 or more threads MASK
  // values in the order of 0xFFFF is appropriate
  private static final int MAX_MASK = 0xFFFF;

  private volatile long mask = 0xF;
  private volatile long lastMaskCheck = System.currentTimeMillis();


   // IMPORTANT: This field can be updated by multiple threads. It follows that
  // its values may *not* be incremented sequentially. However, we don't care
  // about the actual value of the field except that from time to time the
  // expression (invocationCounter++ & mask) == mask) should be true.
  private long invocationCounter = 0;


  // if less than thresholdForMaskIncrease milliseconds elapse between invocations of updateMaskIfNecessary()
  // method, then the mask should be increased
  private static final long thresholdForMaskIncrease = 100;

  // if more than thresholdForMaskDecrease milliseconds elapse between invocations of updateMaskIfNecessary() method,
  // then the mask should be decreased
  private final long thresholdForMaskDecrease = thresholdForMaskIncrease*8;


  public boolean skipFurtherWork() {
     return ((invocationCounter++) & mask) != mask;
  }

  // update the mask so as to execute change detection code about once every 100 to 8000 milliseconds.
  public void updateMaskIfNecessary(long now) {
    final long timeElapsedSinceLastMaskUpdateCheck = now - lastMaskCheck;
    lastMaskCheck = now;
    if (timeElapsedSinceLastMaskUpdateCheck < thresholdForMaskIncrease && (mask < MAX_MASK)) {
      mask = (mask << 1) | 1;
    } else if (timeElapsedSinceLastMaskUpdateCheck > thresholdForMaskDecrease) {
      mask = mask >>> 2;
    }
  }
}
