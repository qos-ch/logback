package ch.qos.logback.classic.pattern;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.qos.logback.classic.pattern.lru.Event;
import ch.qos.logback.classic.pattern.lru.T_LRUCache;

public class LRUCacheTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void smoke() {
    LRUCache<String, String> cache = new LRUCache<String, String>(2);
    cache.put("a", "a");
    cache.put("b", "b");
    cache.put("c", "c");
    List<String> witness = new LinkedList<String>();
    witness.add("b");
    witness.add("c");
    assertEquals(witness, cache.keyList());
  }

  @Test
  public void typicalScenarioTest() {
    int simulationLen = 1000 * 20;
    int cacheSize = 500;
    int worldSize = 10000;
    doScenario(simulationLen, cacheSize, worldSize);
  }

  @Test
  public void scenarioCoverageTest() {
    int simulationLen = 1000 * 20;
    int[] cacheSizes = new int[] {1,5,10,100,1000,5000,10000};
    int[] worldSizes = new int[] {1,10,100,1000,20000};
    for (int i = 0; i < cacheSizes.length; i++) {
      for (int j = 0; j < worldSizes.length; j++) {
        System.out.println("cacheSize="+cacheSizes[i]+", worldSize="+worldSizes[j]);
        doScenario(simulationLen, cacheSizes[i], worldSizes[j]);
      }
    }
  }

  void doScenario(int simulationLen, int chacheSize, int worldSize) {
    int cacheSize = 500;
    int get2PutRatio = 10;

    Simulator simulator = new Simulator(worldSize, get2PutRatio);
    List<Event> scenario = simulator.generateScenario(simulationLen);
    LRUCache<String, String> lruCache = new LRUCache<String, String>(cacheSize);
    T_LRUCache<String> tlruCache = new T_LRUCache<String>(cacheSize);
    simulator.simulate(scenario, lruCache, tlruCache);
    assertEquals(tlruCache.ketList(), lruCache.keyList());
  }
}
