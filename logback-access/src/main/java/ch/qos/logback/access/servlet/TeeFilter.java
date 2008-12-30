package ch.qos.logback.access.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.access.AccessConstants;

public class TeeFilter implements Filter {

  public void destroy() {
    // NOP
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    if (request instanceof HttpServletRequest) {
      try {
        TeeHttpServletRequest teeRequest = new TeeHttpServletRequest(
            (HttpServletRequest) request);
        TeeHttpServletResponse teeResponse = new TeeHttpServletResponse(
            (HttpServletResponse) response);
        
        //System.out.println("BEFORE TeeFilter. filterChain.doFilter()");
        filterChain.doFilter(teeRequest, teeResponse);
        //System.out.println("AFTER TeeFilter. filterChain.doFilter()");

        teeResponse.finish();
        // let the output contents be available for later use by
        // logback-access-logging
        teeRequest.setAttribute(AccessConstants.LB_OUTPUT_BUFFER, teeResponse
            .getOutputBuffer());
      } catch (IOException e) {
        e.printStackTrace();
        throw e;
      } catch (ServletException e) {
        e.printStackTrace();
        throw e;
      }
    } else {
      filterChain.doFilter(request, response);
    }

  }

  public void init(FilterConfig arg0) throws ServletException {
    // NOP
  }

}
