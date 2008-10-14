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
