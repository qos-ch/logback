package ch.qos.logback.access.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
  static final int BUF_SIZE= 128;
  
  public static String readToString(InputStream in) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[BUF_SIZE];
    int n = 0;
    while( (n = in.read(buf, 0, BUF_SIZE)) != -1) {
      baos.write(buf, 0, n);
    }
    return baos.toString();
  }
}
