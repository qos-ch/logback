package ch.qos.logback.core;

import ch.qos.logback.core.spi.LifeCycle;

public class MockLifeCycleComponent implements LifeCycle {

  private boolean started;
  
  public void start() {
    started = true;      
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }
  
}