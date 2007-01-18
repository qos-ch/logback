package ch.qos.logback.access.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class TeeHttpServletResponse extends HttpServletResponseWrapper {

  final TeeServletOutputStream teeServletOutputStream;
  PrintWriter writer;

  public TeeHttpServletResponse(HttpServletResponse httpServletResponse)
      throws IOException {
    super(httpServletResponse);
    ServletOutputStream underlyingStream = httpServletResponse
        .getOutputStream();
    teeServletOutputStream = new TeeServletOutputStream(underlyingStream);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return teeServletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (writer == null)
      writer = new PrintWriter(getOutputStream());
    return writer;
  }

}
