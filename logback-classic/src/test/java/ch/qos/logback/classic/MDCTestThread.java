package ch.qos.logback.classic;

import org.slf4j.MDC;

public class MDCTestThread extends Thread {
  
  String val;
  
  public MDCTestThread(String val) {
    super();
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
    //System.out.println("Exiting "+val);
  }
} 