package ch.qos.logback.access.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class TeeFilter implements Filter {

  public void destroy() {
    // NOP
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    
    if(request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      request =  new TeeHttpServletRequest(httpRequest);
    }
    
    filterChain.doFilter(request, response);

  }

  public void init(FilterConfig arg0) throws ServletException {
    // NOP
  }

}
