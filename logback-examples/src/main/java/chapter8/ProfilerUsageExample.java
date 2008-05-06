package chapter8;

import ch.qos.logback.classic.stopwatch.Profiler;

public class ProfilerUsageExample {

  public static void main(String[] args) {
    Profiler profiler = new Profiler("BASIC");
    profiler.start("A");
    doA();
       
    profiler.start("B");
    for (int i = 0; i < 5; i++) {
      doB(i);
    }
    profiler.start("Other");
    doOther();
    profiler.stop().print();
  }

  static void doA() {
    delay(10);
  }
  
  static void doB(int millis) {
    delay(millis);
  }

  static void doOther() {
    delay(10);
  }

  
  static  void delay(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
    }
  }
}
