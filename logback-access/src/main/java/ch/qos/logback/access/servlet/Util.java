package ch.qos.logback.access.servlet;

import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.access.Constants;

public class Util {

  public static boolean isFormUrlEncoded(HttpServletRequest request) {
    if ("POST".equals(request.getMethod())
        && Constants.X_WWW_FORM_URLECODED.equals(request.getContentType())) {
      return true;
    } else {
      return false;
    }
  }
}
