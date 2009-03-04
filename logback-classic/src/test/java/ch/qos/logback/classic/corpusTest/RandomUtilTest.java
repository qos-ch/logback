package ch.qos.logback.classic.corpusTest;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.corpus.RandomUtil;


public class RandomUtilTest {
  long now = System.currentTimeMillis();
  
  @Before
  public void setup() {
    System.out.println(RandomUtilTest.class.getName()+" now="+now);
  }
  
  @Test
  public void smoke() {
    
    int EXPECTED_AVERAGE = 6;
    int EXPECTED_STD_DEVIATION = 3;
    

    System.out.println();
    Random r = new Random(now);
    int len = 3000;
    int[] valArray = new int[len];
    for(int i = 0; i < len; i++) {
      valArray[i] = RandomUtil.gaussianAsPositiveInt(r, EXPECTED_AVERAGE, EXPECTED_STD_DEVIATION);
    }
    double avg = average(valArray);

    assertEquals(EXPECTED_AVERAGE, avg, 0.3);
  }
  
  public double average(int[] va) {
    double avg = 0;
    for(int i = 0; i < va.length; i++) {
      avg = (avg*i+va[i])/(i+1); 
    }
    return avg;
  }
  
}
