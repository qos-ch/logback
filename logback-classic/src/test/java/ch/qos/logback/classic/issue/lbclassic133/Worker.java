package ch.qos.logback.classic.issue.lbclassic133;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker {
  private static Logger s_logger = LoggerFactory.getLogger(Worker.class);

  private final Object m_lock = new Object();

  public void work() {
    // Locks the object to do some work changing internal status.
    synchronized (m_lock) {
      // Does some work...
      try {
        Thread.sleep(1 * 1000);
      } catch (InterruptedException exc) {
        //
      }
      // Then calls logger, while still holding the lock.
      s_logger.debug("Did some work, result is: {}."/* ,... */);
    }
  }

  public String getStatus() {
    // Locks the object to make sure the status snapshot is coherent. (Will
    // deadlock.)
    synchronized (m_lock) {
      // Packs some status information, e.g. in a StringBuffer.
      final StringBuffer buf = new StringBuffer("STATUS");
      // Returns the string.
      return buf.toString();
    }
  }

  @Override
  public String toString() {
    return getStatus();
  }
}