/**
 * Logback: the generic, reliable, fast and flexible logging framework.
 * 
 * Copyright (C) 2000-2008, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.net;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class LoginAuthenticator extends Authenticator {

  String username;
  String password;
  
  LoginAuthenticator(String username, String password) {
    this.username = username;
    this.password = password;
  }
  
  public PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(username, password);
}

}
