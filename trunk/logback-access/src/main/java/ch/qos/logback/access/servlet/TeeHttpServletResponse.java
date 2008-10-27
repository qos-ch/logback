package ch.qos.logback.access.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TeeHttpServletResponse extends HttpServletResponseWrapper {

  TeeServletOutputStream teeServletOutputStream;
  PrintWriter teeWriter;

  public TeeHttpServletResponse(HttpServletResponse httpServletResponse) {
    super(httpServletResponse);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (teeServletOutputStream == null) {
      teeServletOutputStream = new TeeServletOutputStream(this.getResponse());
    }
    return teeServletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (this.teeWriter == null) {
      this.teeWriter = new PrintWriter(new OutputStreamWriter(getOutputStream()),
          true);
    }
    return this.teeWriter;
  }

  @Override
  public void flushBuffer() {
    if (this.teeWriter != null) {
      this.teeWriter.flush();
    }
  }

  byte[] getOutputBuffer() {
    // teeServletOutputStream can be null if the getOutputStream method is never
    // called.
    if (teeServletOutputStream != null) {
      return teeServletOutputStream.getOutputStreamAsByteArray();
    } else {
      return null;
    }
  }

  void finish() throws IOException {
    if (this.teeWriter != null) {
      this.teeWriter.close();
    }
    if (this.teeServletOutputStream != null) {
      this.teeServletOutputStream.close();
    }
  }
}
