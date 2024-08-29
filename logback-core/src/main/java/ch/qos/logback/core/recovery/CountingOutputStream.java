package ch.qos.logback.core.recovery;

import java.io.IOException;
import java.io.OutputStream;

class CountingOutputStream extends OutputStream {

  private final OutputStream delegate;

  private long count;

  CountingOutputStream(OutputStream delegate) {
    this.delegate = delegate;
  }

  public long getCount() {
    return count;
  }

  @Override
  public void write(int b) throws IOException {
    delegate.write(b);
    count++;
  }

  @Override
  public void write(byte[] b) throws IOException {
    delegate.write(b);
    count += b.length;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    delegate.write(b, off, len);
    count += len;
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

}
