package ch.qos.logback.classic.issue.lbcore224;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.StatusPrinter;
import org.apache.zookeeper.test.QuorumUtil;
import org.junit.After;
import org.junit.Test;
import org.slf4j.LoggerFactory;


public class LBCORE224Test {

  final QuorumUtil qU = new QuorumUtil(1);

  @After
  public void teatDown() throws Exception {
    Context context = (Context) LoggerFactory.getILoggerFactory();
    StatusPrinter.print(context);
    qU.tearDown();
  }

  @Test
  public void test() throws Exception {
    for (int i = 0; i < 10; i++) {
      qU.startQuorum();
      qU.shutdownAll();
      System.out.println(i);
    }
  }
}
