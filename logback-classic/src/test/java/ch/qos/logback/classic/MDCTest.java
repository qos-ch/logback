package ch.qos.logback.classic;

import junit.framework.TestCase;

public class MDCTest extends TestCase {


  public void test() throws InterruptedException {
    TestThread threadA = new TestThread("a");
    threadA.start();
    
    TestThread threadB = new TestThread("b");
    threadB.start();
    
    threadA.join();
    threadB.join();
    
    
    assertNull(threadA.x0);
    assertEquals("a", threadA.x1);
    assertNull(threadA.x2);
    
    assertNull(threadB.x0);
    assertEquals("b", threadB.x1);
    assertNull(threadB.x2);
    
  }
  
}

class TestThread extends Thread {
  
  String val;
  TestThread(String val) {
    this.val = val;
  }
  String x0;
  String x1;
  String x2;
  
  public void run() {
    x0 = MDC.get("x");
    MDC.put("x", val);
    x1 = MDC.get("x");
    MDC.clear();
    x2 = MDC.get("x");
    System.out.println("Exiting "+val);
  }
}