package ch.qos.logback.classic.turbo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;

public class LRUMessageCacheTest {
  private static final int INVOCATIONS_PER_TASK = 500 * 1024;
  private static final int THREADS_NUMBER = 16;

  @Test
  public void testEldestEntriesRemoval() {
    final LRUMessageCache cache = new LRUMessageCache(2);
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
    Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("0"));
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
    Assert.assertEquals(1, cache.getMessageCountAndThenIncrement("1"));
    // 0 entry should have been removed.
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("2"));
    // So it is expected a returned value of 0 instead of 2.
    // 1 entry should have been removed.
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("0"));
    // So it is expected a returned value of 0 instead of 2.
    // 2 entry should have been removed.
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("1"));
    // So it is expected a returned value of 0 instead of 2.
    Assert.assertEquals(0, cache.getMessageCountAndThenIncrement("2"));
  }

  @Test
  public void multiThreadsTest() throws InterruptedException, ExecutionException {
    final LRUMessageCache cache = new LRUMessageCache(THREADS_NUMBER);

    ArrayList<TestTask> tasks = new ArrayList<TestTask>(THREADS_NUMBER);
    for (int i = 0; i < THREADS_NUMBER; i++) {
      tasks.add(new TestTask(cache));
    }

    ExecutorService execSrv = Executors.newFixedThreadPool(THREADS_NUMBER);

    List<Future<Boolean>> futures = execSrv.invokeAll(tasks);
    for (Future<Boolean> future : futures) {
      // Validate that task has successfully finished.
      future.get();
    }
  }

  /**
   * Each thread is using always the same "Message" key.
   */
  private class TestTask implements Callable<Boolean> {
    private int prevValue = -1;
    private final LRUMessageCache cache;

    public TestTask(LRUMessageCache cache) {
      this.cache = cache;
    }

    public Boolean call() throws Exception {
      String msg = Thread.currentThread().getName();

      for (int i = 0; i < INVOCATIONS_PER_TASK; i++) {
        int current = cache.getMessageCountAndThenIncrement(msg);
        // Ensure that new count is greater than previous count.
        Assert.assertEquals(prevValue + 1, current);
        prevValue = current;
      }

      return true;
    }
  }
}
