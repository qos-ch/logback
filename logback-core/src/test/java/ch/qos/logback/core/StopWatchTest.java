package ch.qos.logback.core;

import junit.framework.TestCase;

public class StopWatchTest extends TestCase {

  public StopWatchTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testBasic() throws InterruptedException {
    StopWatch sw = new StopWatch("testBasic");
    
    Thread.sleep(55100);
    System.out.println(sw.stop().toString());
    
    
  }

}
