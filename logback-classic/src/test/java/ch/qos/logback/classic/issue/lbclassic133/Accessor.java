package ch.qos.logback.classic.issue.lbclassic133;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Accessor {
  private static Logger s_logger = LoggerFactory.getLogger(Accessor.class);

  public void access(Worker worker) {
    /**
     * at some point, for any reason, this method decides to log the status of
     * the worker object.
     */
    s_logger.debug("Current worker status is: {}.", worker);
    /**
     * the following line would not cause the deadlock, because the thread never
     * tries to acquire a lock on worker while already having a lock on the
     * logger.
     */
    //s_logger.debug("Current worker status (not deadlocking) is: {}.", worker
    //    .toString());
  }
}
