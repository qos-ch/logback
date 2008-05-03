package ch.qos.logback.classic.stopwatch;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;

public class ProfilerTest {

  Logger logger = LoggerFactory.getLogger(ProfilerTest.class);
  
  @Test
  public void smoke() {
    Profiler profiler = new Profiler("SMOKE");
    System.out.println("Hello");
    profiler.stop();
  }

  @Test
  public void X() {
    StatusManager sm = new BasicStatusManager();
    Status g = new InfoStatus("global", this);
    Status g1 = new InfoStatus("g1", this);
    Status g2 = new InfoStatus("g2", this);
    Status g11 = new InfoStatus("g11", this);
    Status g12 = new InfoStatus("g11", this);
    Status g21 = new InfoStatus("g21", this);
    
    
    g.add(g1);
    g.add(g2);
    
    g1.add(g11);
    g1.add(g12);
    g2.add(g21);
    
    sm.add(g);
    StatusPrinter.print(sm);
    
  }
  @Test
  public void testBasicProfiling() {
    Profiler profiler = new Profiler("BAS");
  
    profiler.start("doX");
    doX(1);
    
    profiler.start("doYYYYY");
    for(int i = 0; i < 5; i++) {
      doY(i);
    }
    profiler.start("doZ");
    doZ(2);
    profiler.stop();
  }
  
  void doX(int millis) {
    delay(millis);
  }
  
  void doY(int millis) {
    delay(millis);    
  }
 
  void doZ(int millis) {
    delay(millis);    
  }
 
  void delay(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
  }
}
