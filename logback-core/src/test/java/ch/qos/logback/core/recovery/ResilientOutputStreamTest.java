package ch.qos.logback.core.recovery;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.CoreTestConstants;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ResilientOutputStreamTest {

  int diff = RandomUtil.getPositiveInt();
  Context context = new ContextBase();


   @Test
   public void verifyRecuperationAfterFailure() throws Exception {
     File dir = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
     dir.mkdirs();
     File file = new File(CoreTestConstants.OUTPUT_DIR_PREFIX+"resilient"+diff+".log");
     ResilientFileOutputStream rfos = new ResilientFileOutputStream(file, true);
     rfos.setContext(context);

     ResilientFileOutputStream spy = spy(rfos);

     spy.write("a".getBytes());
     spy.flush();

     spy.getChannel().close();
     spy.write("b".getBytes());
     spy.flush();
     Thread.sleep(RecoveryCoordinator.BACKOFF_COEFFICIENT_MIN+10);
     spy.write("c".getBytes());
     spy.flush();
     verify(spy).openNewOutputStream();

   }

}
