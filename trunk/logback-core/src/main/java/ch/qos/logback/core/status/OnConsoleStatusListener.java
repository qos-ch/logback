package ch.qos.logback.core.status;

import ch.qos.logback.core.util.StatusPrinter;

public class OnConsoleStatusListener implements StatusListener {

  public void addStatusEvent(Status status) {
    StringBuilder sb = new StringBuilder();
    StatusPrinter.buildStr(sb, "", status);
    System.out.println(sb);
  }
}
