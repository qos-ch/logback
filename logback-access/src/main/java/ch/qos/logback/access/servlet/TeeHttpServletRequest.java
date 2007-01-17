package ch.qos.logback.access.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ch.qos.logback.access.Constants;

/**
 * As the "tee" program on Unix, duplicate the request's input stream.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
class TeeHttpServletRequest extends HttpServletRequestWrapper {

  private TeeServletInputStream inStream;
  private BufferedReader reader;

  TeeHttpServletRequest(HttpServletRequest request) {
    super(request);
    inStream = new TeeServletInputStream(request);
    // add the contents of the input buffer as an attribute of the request in byte[] format
    request.setAttribute(Constants.LB_INPUT_BUFFER, inStream.getInputBuffer());
    reader = new BufferedReader(new InputStreamReader(inStream));
  }
  
  byte[] getInputBuffer() {
    return inStream.getInputBuffer();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return inStream;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return reader;
  }
  
}
