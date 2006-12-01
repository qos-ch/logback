package ch.qos.logback.classic.util;

import java.io.File;
import java.util.Properties;

public class Constants {
	
  public static final String TEST_DIR_PREFIX;
  
  static {
    
    Properties p = System.getProperties();
    for(Object o: p.keySet()) {
      System.out.println(o+"="+p.get(o));
    }
    File f = new File("");
    String path = "";
    String absolute = f.getAbsolutePath();
    if (!absolute.endsWith("logback-classic")) {
      path = "logback-classic/";
    }
    TEST_DIR_PREFIX = path + "src/test/";
  }



}
