package ch.qos.logback.access.dummy;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DummyServletOutputStream extends ServletOutputStream {

  private final OutputStream targetStream;

  public DummyServletOutputStream(OutputStream targetStream) {
    this.targetStream = targetStream;
  }

  @Override
  public void write(int b) throws IOException {
    this.targetStream.write(b);
  }

  public void flush() throws IOException {
    super.flush();
    this.targetStream.flush();
  }

  public void close() throws IOException {
    super.close();
    this.targetStream.close();
  }
}
