package ch.qos.logback.access.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {

  public static String readToString(InputStream in) throws IOException {
    StringBuffer sb = new StringBuffer();
    BufferedReader inbr = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = inbr.readLine()) != null) {
      sb.append(line);
    }

    return sb.toString();
  }
}
