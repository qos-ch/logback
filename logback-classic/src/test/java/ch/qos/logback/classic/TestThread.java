package ch.qos.logback.classic;

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