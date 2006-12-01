package ch.qos.logback.core.util;

import java.io.File;

public class Constants {
  
  static {
    File f = new File("");
    String path = "";
    String absolute = f.getAbsolutePath();
    if (!absolute.endsWith("logback-core")) {
      path = "logback-core/";
    }
    TEST_DIR_PREFIX = path + "src/test/";
  }

  public static final String TEST_DIR_PREFIX;

}
