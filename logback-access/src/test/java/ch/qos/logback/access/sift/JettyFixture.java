/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.sift;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.ByteArrayISO8859Writer;

import ch.qos.logback.access.jetty.JettyFixtureBase;
import ch.qos.logback.access.jetty.RequestLogImpl;

public class JettyFixture extends JettyFixtureBase {

  Handler handler = new BasicHandler();

  public JettyFixture(RequestLogImpl impl, int port) {
    super(impl, port);
  }

  @Override
  protected void buildContext() {
    requestLogImpl.start();
  }

  @Override
  protected Handler getHandler() {
    return handler;
  }

  class BasicHandler extends AbstractHandler {
    public void handle(String target, HttpServletRequest request,
        HttpServletResponse response, int dispatch) throws IOException,
        ServletException {

      // String requestContent = Util.readToString(request.getInputStream());
      // System.out.println("request content: " + requestContent);

      OutputStream out = response.getOutputStream();
      ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
      writer.write("hello world");
      writer.flush();
      response.setContentLength(writer.size());
      writer.writeTo(out);
      out.flush();

      Request base_request = (request instanceof Request) ? (Request) request
          : HttpConnection.getCurrentConnection().getRequest();
      base_request.setHandled(true);

    }
  }
}
