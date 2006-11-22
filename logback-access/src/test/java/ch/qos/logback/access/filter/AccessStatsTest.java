package ch.qos.logback.access.filter;

import junit.framework.TestCase;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.util.TimeUtil;

public class AccessStatsTest extends TestCase {

  AccessEvent accessEvent = new AccessEvent(null, null, null);
  public AccessStatsTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBasic() {
    //CountingFilter cf = new CountingFilter();
    //AccessStatsImpl asi = new AccessStatsImpl(cf);
  }
}
