package ch.qos.logback.access.net;

import java.io.IOException;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import ch.qos.logback.access.pattern.helpers.DummyRequest;
import ch.qos.logback.access.pattern.helpers.DummyResponse;
import ch.qos.logback.access.pattern.helpers.DummyServerAdapter;
import ch.qos.logback.access.spi.AccessEvent;

public class SerializationPerfTest extends TestCase {

  ObjectOutputStream oos;
  
  int loopNumber = 10000;
  int resetFrequency = 100;
  int pauseFrequency = 10;
  long pauseLengthInMillis = 20;
  

  public void setUp() throws Exception {
    super.setUp();
    oos = new ObjectOutputStream(new NOPOutputStream());

  }

  public void tearDown() throws Exception {
    super.tearDown();
    oos.close();
    oos = null;
  }
  
  public void test1() throws Exception {
    // first run for just in time compiler
    int resetCounter = 0;
    int pauseCounter = 0;
    for (int i = 0; i < loopNumber; i++) {
      try {
        AccessEvent ae = buildNewAccessEvent();
        //average time for the next method: 5000 nanos
        ae.prepareForDeferredProcessing();
        oos.writeObject(ae);
        oos.flush();
        if (++resetCounter >= resetFrequency) {
          oos.reset();
          resetCounter = 0;
        }
        if (++pauseCounter >= pauseFrequency) {
          Thread.sleep(pauseLengthInMillis);
          pauseCounter = 0;
        }

      } catch (IOException ex) {
        fail(ex.getMessage());
      }
    }
    
    // second run
    Long t1;
    Long t2;
    Long total = 0L;
    resetCounter = 0;
    pauseCounter = 0;
    // System.out.println("Beginning mesured run");
    for (int i = 0; i < loopNumber; i++) {
      try {
        AccessEvent ae = buildNewAccessEvent();
        t1 = System.nanoTime();
        //average length of the next method: 4000 nanos
        ae.prepareForDeferredProcessing();
        oos.writeObject(ae);
        oos.flush();
        t2 = System.nanoTime();
        total += (t2 - t1);
        if (++resetCounter >= resetFrequency) {
          oos.reset();
          resetCounter = 0;
        }
        if (++pauseCounter >= pauseFrequency) {
          Thread.sleep(pauseLengthInMillis);
          pauseCounter = 0;
        }
      } catch (IOException ex) {
        fail(ex.getMessage());
      }
    }
    
    total /= (1000);//nanos -> micros
    System.out.println("Loop done : average time = " + total / loopNumber
        + " microsecs after " + loopNumber + " writes.");
    //average time: 26-30 microsec = 0.034 milis
  }
  
  private AccessEvent buildNewAccessEvent() {
    DummyRequest request = new DummyRequest();
    DummyResponse response = new DummyResponse();
    DummyServerAdapter adapter = new DummyServerAdapter(request, response);
    
    AccessEvent ae = new AccessEvent(request, response, adapter);
    return ae;
  }

}
