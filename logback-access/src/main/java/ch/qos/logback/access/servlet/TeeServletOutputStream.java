package ch.qos.logback.access.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class TeeServletOutputStream extends ServletOutputStream {

  final ServletOutputStream underlyingStream;
  final ByteArrayOutputStream baos;

  TeeServletOutputStream(ServletOutputStream underlyingStream) {
    this.underlyingStream = underlyingStream;
    baos = new ByteArrayOutputStream();
  }

  @Override
  public void write(int val) throws IOException {
    underlyingStream.write(val);
    baos.write(val);
  }

  @Override
  public void write(byte[] byteArray) throws IOException {
    underlyingStream.write(byteArray);
    baos.write(byteArray);
  }

  @Override
  public void write(byte byteArray[], int offset, int length)
      throws IOException {
    underlyingStream.write(byteArray, offset, length);
    baos.write(byteArray, offset, length);
  }

  public void close() throws IOException {
    underlyingStream.close();
    baos.close();
  }

  public void flush() throws IOException {
    underlyingStream.flush();
    baos.flush();
  }

}
