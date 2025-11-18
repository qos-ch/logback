package ch.qos.logback.classic.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.FilterReply;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class BurstFilterTest {

  private BurstFilter burstFilter = new BurstFilter();
  @Test
  public void testLess(){
    burstFilter.setLevel(Level.INFO);
    burstFilter.setMaxBurst(200);
    burstFilter.setRate(10f);
    burstFilter.start();
    FilterReply result = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result);
  }

  @Test
  public void testLevelDebug(){
    burstFilter.setLevel(Level.INFO);
    burstFilter.setMaxBurst(200);
    burstFilter.setRate(10f);
    burstFilter.start();
    FilterReply result = burstFilter.decide(null, null, Level.DEBUG, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result);
  }

  @Test
  public void testMore(){
    burstFilter.setLevel(Level.INFO);
    burstFilter.setMaxBurst(2);
    burstFilter.setRate(1);
    burstFilter.start();
    FilterReply result = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result);
    FilterReply result2 = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result2);
    FilterReply result3 = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.DENY, result3);
    FilterReply result4 = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.DENY, result4);
  }
  @Test
  public void testMoreWithLevels(){
    burstFilter.setLevel(Level.INFO);
    burstFilter.setLevel(Level.WARN);
    burstFilter.setMaxBurst(2);
    burstFilter.setRate(1);
    burstFilter.start();
    FilterReply result = burstFilter.decide(null, null, Level.WARN, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result);
    FilterReply result2 = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result2);
    FilterReply result3 = burstFilter.decide(null, null, Level.WARN, null, null, null);
    Assert.assertEquals(FilterReply.DENY, result3);
    FilterReply result4 = burstFilter.decide(null, null, Level.INFO, null, null, null);
    Assert.assertEquals(FilterReply.DENY, result4);
    FilterReply result5 = burstFilter.decide(null, null, Level.ERROR, null, null, null);
    Assert.assertEquals(FilterReply.NEUTRAL, result5);
  }

  @After
  public void clean(){
    burstFilter.stop();
  }
}
