package ch.qos.logback.classic.joran;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusListener;

public class TrivialStatusListener implements StatusListener {

  List<Status> list = new ArrayList<Status>();
  
  public void addStatusEvent(Status status) {
    list.add(status);
  }

}
