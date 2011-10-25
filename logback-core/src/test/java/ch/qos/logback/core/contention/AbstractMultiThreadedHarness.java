package ch.qos.logback.core.contention;

abstract public class AbstractMultiThreadedHarness {

  RunnableWithCounterAndDone[] runnableArray;

  abstract void waitUntilEndCondition() throws InterruptedException;

  public void execute(RunnableWithCounterAndDone[] runnableArray)
      throws InterruptedException {
    this.runnableArray = runnableArray;
    Thread[] threadArray = new Thread[runnableArray.length];

    for (int i = 0; i < runnableArray.length; i++) {
      threadArray[i] = new Thread(runnableArray[i], "Harness["+i+"]");
    }
    for (Thread t : threadArray) {
      t.start();
    }

    waitUntilEndCondition();
    for (RunnableWithCounterAndDone r : runnableArray) {
      r.setDone(true);
    }
    for (Thread t : threadArray) {
      t.join();
    }
  }
}
