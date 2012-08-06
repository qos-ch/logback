package ch.qos.logback.core.contention;

import java.util.concurrent.ThreadPoolExecutor;

public class WaitOnExecutionMultiThreadedHarness extends AbstractMultiThreadedHarness {
    ThreadPoolExecutor threadPoolExecutor;
    int count;

    public WaitOnExecutionMultiThreadedHarness(ThreadPoolExecutor threadPoolExecutor, int count) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.count = count;

    }
    @Override
    void waitUntilEndCondition() throws InterruptedException {
      while(threadPoolExecutor.getCompletedTaskCount() < count) {
        Thread.yield();
      }
    }
}
