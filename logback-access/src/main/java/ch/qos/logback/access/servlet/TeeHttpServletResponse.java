package ch.qos.logback.access.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TeeHttpServletResponse extends HttpServletResponseWrapper {

  TeeServletOutputStream teeServletOutputStream;
  PrintWriter writer;

  public TeeHttpServletResponse(HttpServletResponse httpServletResponse) {
    super(httpServletResponse);
    //System.out.println("TeeHttpServletResponse.constructor called");
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    //System.out.println("TeeHttpServletResponse.getOutputStream() called");
    if(teeServletOutputStream == null) {
      teeServletOutputStream = new TeeServletOutputStream(
          this.getResponse());
    }
    return teeServletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    //System.out.println("TeeHttpServletResponse.getWriter() called");
    if (this.writer == null) {
      this.writer = new PrintWriter(new OutputStreamWriter(getOutputStream()), true);
    }
    return this.writer;
  }

  @Override
  public void flushBuffer() {
    //System.out.println("TeeHttpServletResponse.flushBuffer() called");
    this.writer.flush();
  }
  
  byte[] getOutputBuffer() {
    return teeServletOutputStream.getOutputBuffer();
  }
  
  
  void  finish() throws IOException {
    this.writer.close();
    teeServletOutputStream.close();
  }
}
