package ch.qos.logback.access.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * As the "tee" program on Unix, duplicate the request's input stream.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class TeeHttpServletRequest extends HttpServletRequestWrapper {

  static final int BUF_SIZE = 512;

  private ServletInputStream inStream;
  private BufferedReader reader;

  public TeeHttpServletRequest(HttpServletRequest request) {
    super(request);
    inStream = new TeeServletInputStream(request);
    reader = new BufferedReader(new InputStreamReader(inStream));
  }

  public ServletInputStream getInputStream() throws IOException {
    return inStream;
  }

  public BufferedReader getReader() throws IOException {
    return reader;
  }

}
