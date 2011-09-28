package ch.qos.logback.classic.issue.lbcore224;

import org.apache.zookeeper.test.QuorumUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



// This test requires zookeeper 3.4.0 built from SVN trunk and setting
// "build.test.dir" system property
public class WithZookeperTest {

  static final String BUILT_TEST_DIR_PROP_KEY = "build.test.dir";
  QuorumUtil qU;

  @Before
  public void before() {
    System.setProperty(BUILT_TEST_DIR_PROP_KEY, "/tmp/zoo");
    qU = new QuorumUtil(1);
  }

  @After
  public void after() throws Exception {
    qU.tearDown();
    System.clearProperty(BUILT_TEST_DIR_PROP_KEY);
  }

  @Test
  public void shouldNotThrowIllegalMonitorStateException () throws Exception {
    for (int i = 0; i < 10; i++) {
      qU.startQuorum();
      qU.shutdownAll();
      System.out.println("iteration "+i);
    }
  }
}
