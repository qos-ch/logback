package ch.qos.logback.classic.issue.lbclassic323;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;

public class Barebones {

  public static void main(String[] args) {
    Context context = new ContextBase();
    for(int i = 0; i < 3; i++) {
      SenderRunnable senderRunnable = new SenderRunnable(""+i);
      context.getExecutorService().execute(senderRunnable);
    }
    System.out.println("done");
    //System.exit(0);
  }

  static class SenderRunnable implements Runnable {
    String id;
    SenderRunnable(String id) {
      this.id = id;
    }

    public void run() {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }
      System.out.println("SenderRunnable " +id);
    }
  }
}
