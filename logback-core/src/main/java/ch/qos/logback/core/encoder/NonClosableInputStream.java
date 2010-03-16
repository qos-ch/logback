package ch.qos.logback.core.encoder;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NonClosableInputStream extends FilterInputStream {

  NonClosableInputStream(InputStream is) {
    super(is);
  }

  /**
   * The whole point of this input stream is to ignore invocations to close()
   */
  @Override
  public void close() {

  }

  public void realClose() throws IOException {
    super.close();
  }

}
