package ch.qos.logback.core.status;

import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;
import ch.qos.logback.core.util.StatusPrinter;

import java.io.PrintStream;
import java.util.List;

/**
 *  Print all new incoming status messages on the on the designated PrintStream.
 * @author Ceki G&uuml;c&uuml;
 */
abstract class OnPrintStreamStatusListenerBase extends ContextAwareBase implements StatusListener, LifeCycle {

  boolean isStarted = false;

  static final long DEFAULT_RESTROSPECTIVE = 300;
  long retrospective = DEFAULT_RESTROSPECTIVE;


  /**
   * The PrintStream used by derived classes
   * @return
   */
  abstract protected PrintStream getPrintStream();

  private void print(Status status) {
    StringBuilder sb = new StringBuilder();
    StatusPrinter.buildStr(sb, "", status);
    getPrintStream().print(sb);
  }

  public void addStatusEvent(Status status) {
    if (!isStarted)
      return;
    print(status);
  }

  /**
   * Print status messages retrospectively
   */
  private void retrospectivePrint() {
    if(context == null)
      return;
    long now = System.currentTimeMillis();
    StatusManager sm = context.getStatusManager();
    List<Status> statusList = sm.getCopyOfStatusList();
    for (Status status : statusList) {
      long timestamp = status.getDate();
      if (now - timestamp < retrospective) {
        print(status);
      }
    }
  }

  public void start() {
    isStarted = true;
    if (retrospective > 0) {
      retrospectivePrint();
    }
  }

  public void setRetrospective(long retrospective) {
    this.retrospective = retrospective;
  }

  public long getRetrospective() {
    return retrospective;
  }

  public void stop() {
    isStarted = false;
  }

  public boolean isStarted() {
    return isStarted;
  }

}
