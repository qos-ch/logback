package ch.qos.logback.core.testUtil;

import ch.qos.logback.core.util.CoreTestConstants;

import java.io.File;

import static junit.framework.Assert.assertTrue;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class FileTestUtil {

  public static void makeTestOutputDir() {
    File target = new File(CoreTestConstants.TARGET_DIR);
    if(target.exists() && target.isDirectory()) {
      File testoutput = new File(CoreTestConstants.OUTPUT_DIR_PREFIX);
      if(!testoutput.exists())
        assertTrue(testoutput.mkdir());
    } else {
      throw new IllegalStateException(CoreTestConstants.TARGET_DIR + " does not exist");
    }
  }
}
