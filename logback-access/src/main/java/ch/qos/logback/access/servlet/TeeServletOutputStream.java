package ch.qos.logback.access.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

public class TeeServletOutputStream extends ServletOutputStream {

  final ServletOutputStream underlyingStream;
  final ByteArrayOutputStream baos;

  TeeServletOutputStream(ServletResponse httpServletResponse)
      throws IOException {
    // System.out.println("TeeServletOutputStream.constructor() called");
    this.underlyingStream = httpServletResponse.getOutputStream();
    if (underlyingStream == null) {
      System.out.println("XXXXX underlyingStream == null");
    } else {
      System.out.println("XXXXX underlyingStream != null");
    }
    baos = new ByteArrayOutputStream();
  }

  byte[] getOutputBuffer() {
    return baos.toByteArray();
  }

  @Override
  public void write(int val) throws IOException {
    // System.out.println("XXXXXXXXXXXWRITE TeeServletOutputStream.write(int)
    // called");
    if (underlyingStream != null) {
      underlyingStream.write(val);
      baos.write(val);
    }
  }

  @Override
  public void write(byte[] byteArray) throws IOException {
    if (underlyingStream == null) {
      return;
    }
    // System.out.println("WRITE TeeServletOutputStream.write(byte[]) called");
    write(byteArray, 0, byteArray.length);
  }

  @Override
  public void write(byte byteArray[], int offset, int length)
      throws IOException {
    if (underlyingStream == null) {
      return;
    }
    // System.out.println("WRITE TeeServletOutputStream.write(byte[], int, int)
    // called");
    // System.out.println(new String(byteArray, offset, length));
    underlyingStream.write(byteArray, offset, length);
    baos.write(byteArray, offset, length);
  }

  public void close() throws IOException {
    // System.out.println("CLOSE TeeServletOutputStream.close() called");

    // If the servlet accessing the stream is using a writer instead of
    // an OutputStream, it will probably call os.close() begore calling
    // writer.close. Thus, the undelying output stream will be called
    // before the data sent to the writer could be flushed.
  }

  public void finish() throws IOException {
    // System.out.println("FINISH TeeServletOutputStream.close() called");
    flush();
    underlyingStream.close();
    baos.close();
  }

  public void flush() throws IOException {
    if (underlyingStream == null) {
      return;
    }
    // System.out.println("FLUSH TeeServletOutputStream.flush() called");
    underlyingStream.flush();
    baos.flush();
  }

}
