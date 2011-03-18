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

}
