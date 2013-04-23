package ch.qos.logback.core.net.server;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class MockExecutorService extends AbstractExecutorService {

  private Runnable lastCommand;
  
  public Runnable getLastCommand() {
    return lastCommand;
  }
  
  public void shutdown() {
  }

  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  public boolean isShutdown() {
    return true;
  }

  public boolean isTerminated() {
    return true;
  }

  public boolean awaitTermination(long timeout, TimeUnit unit)
      throws InterruptedException {
    return true;
  }

  public void execute(Runnable command) {
    command.run();
    lastCommand = command;
  }
  
}