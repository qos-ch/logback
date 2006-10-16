package ch.qos.logback.core.joran;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.action.IncAction;
import ch.qos.logback.core.util.Constants;

public class TrivialcConfiguratorTest extends TestCase {

  Context context = new ContextBase();
  
  public TrivialcConfiguratorTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void doTest(String filename) throws Exception {

    TrivialConfigurator gc = new TrivialConfigurator();
    gc.setContext(context);
    gc.doConfigure(Constants.TEST_DIR_PREFIX
        + "input/joran/"+ filename);
  }
  
  public void test() throws Exception {
    int oldBeginCount = IncAction.beginCount;
    int oldEndCount = IncAction.endCount;
    int oldErrorCount = IncAction.errorCount;
    doTest("inc.xml");
    assertEquals(oldErrorCount, IncAction.errorCount);
    assertEquals(oldBeginCount+1, IncAction.beginCount);
    assertEquals(oldEndCount+1, IncAction.endCount);
  }

}
