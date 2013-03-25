package ch.qos.logback.classic.net;

import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.ssl.SSLConfiguration;

public class SSLSocketServer extends SocketServer {

  private SSLConfiguration ssl;

  /**
   * Creates a secure (SSL) socket server.
   * @param lc logger context for the server
   * @return socket server or {@code null} if a server socket could not
   *    be created due to an error
   */
  @Override
  protected SimpleSocketServer createSocketServer(LoggerContext lc) {
    try {
      SSLContext sslContext = getSsl().createContext(this);
      SSLParameters sslParameters = getSsl().createParameters(
          sslContext, this);
      return new SimpleSSLSocketServer(lc, getPort(), getBacklog(), 
          getInetAddress(), sslContext, sslParameters);
    }
    catch (UnknownHostException ex) {
      addError(getAddress() + ": unknown host");
      return null;
    }
    catch (Exception ex) {
      addError(ex.getMessage());
      return null;
    }
  }

  /**
   * Sets the server's SSL configuration.
   * @return SSL configuration or {@code null} if no SSL configuration was
   *    provided
   */
  public SSLConfiguration getSsl() {
    return ssl;
  }

  /**
   * Gets the server's SSL configuration.
   * @param ssl the SSL configuration to set.
   */
  public void setSsl(SSLConfiguration ssl) {
    this.ssl = ssl;
  }


}
