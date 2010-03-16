package ch.qos.logback.core.appender;

import java.io.IOException;
import java.io.PrintStream;

import ch.qos.logback.core.util.TeeOutputStream;

public class XTeeOutputStream extends TeeOutputStream {

  boolean closed = false;
  public XTeeOutputStream(PrintStream targetPS) {
    super(targetPS);
  }

  @Override
  public void close() throws IOException {
    closed = true;
    super.close();
  }
  
  
  public boolean isClosed() {
    return closed;
  }
}
