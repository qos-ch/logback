package ch.qos.logback.classic.util;

import java.io.File;

public class Constants {
	
  static {
    File f = new File("");
    String path = "";
    String absolute = f.getAbsolutePath();
    if (!absolute.endsWith("logback-classic")) {
      path = "logback-classic/";
    }
    TEST_DIR_PREFIX = path + "src/test/";
  }

  public static final String TEST_DIR_PREFIX;

}
