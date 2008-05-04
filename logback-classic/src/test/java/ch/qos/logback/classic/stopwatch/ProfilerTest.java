package ch.qos.logback.classic.stopwatch;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfilerTest {

  Logger logger = LoggerFactory.getLogger(ProfilerTest.class);

  @Test
  public void smoke() {
    Profiler profiler = new Profiler("SMOKE");
    System.out.println("Hello");
    profiler.stop();
  }

  @Ignore
  @Test
  public void testBasicProfiling() {
    Profiler profiler = new Profiler("BAS");

    profiler.start("doX");
    doX(1);

    profiler.start("doYYYYY");
    for (int i = 0; i < 5; i++) {
      doY(i);
    }
    profiler.start("doZ");
    doZ(2);
    profiler.stop().print();
  }

  @Test
  public void testNestedProfiling() {
    Profiler profiler = new Profiler("BAS");

    profiler.start("doX");
    doX(1);

    profiler.start("doYYYYY");
    for (int i = 0; i < 5; i++) {
      doY(i);
    }
    Profiler nested = profiler.startNested("subtask");
    doSubtask(nested);
    profiler.start("doZ");
    doZ(2);
    profiler.stop().print();
  }

  void doX(int millis) {
    delay(millis);
  }

  public void doSubtask(Profiler nested) {
    nested.start("n1");
    doX(1);
    
    nested.start("n2");
    doX(5);
    nested.stop();
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
