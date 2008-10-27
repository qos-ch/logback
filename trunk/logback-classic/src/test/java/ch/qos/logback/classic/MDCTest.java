package ch.qos.logback.classic;

import junit.framework.TestCase;

public class MDCTest extends TestCase {


  public void test() throws InterruptedException {
    MDCTestThread threadA = new MDCTestThread("a");
    threadA.start();
    
    MDCTestThread threadB = new MDCTestThread("b");
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
