package ch.qos.logback.access.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class TeeServletInputStream extends ServletInputStream {

  InputStream in;
  byte[] inputBuffer;
  
  TeeServletInputStream(HttpServletRequest request) {
    duplicateInputStream(request);
  }
  
  @Override
  public int read() throws IOException {
    return in.read();
  }

  private void duplicateInputStream(HttpServletRequest request) {
    try {
      int len = request.getContentLength();
      ServletInputStream originalSIS = request.getInputStream();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      inputBuffer = new byte[len];
      int n = 0;
      while ((n = originalSIS.read(inputBuffer, 0, len)) != -1) {
        baos.write(inputBuffer, 0, n);
      }
      this.in = new ByteArrayInputStream(inputBuffer);
      originalSIS.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public byte[] getInputBuffer() {
    return inputBuffer;
  }
}
