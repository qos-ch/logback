package ch.qos.logback.core.net.ssl;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.net.ConnectionRunner;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.util.Duration;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;

public class SSLConnectionRunner extends ConnectionRunner {

  private SocketFactory sslSocketFactory;

  public SSLConnectionRunner(ContextAware contextAware, String remoteHost, int port,
                             Duration reconnectDuration, SSLConfiguration ssl) throws GeneralSecurityException {
    super(contextAware, remoteHost, port, reconnectDuration);
    initSocketFactory(ssl);
  }

  void initSocketFactory(SSLConfiguration ssl) throws GeneralSecurityException {
    SSLContext sslContext = ssl.createContext(this);
    SSLParametersConfiguration parameters = ssl.getParameters();
    parameters.setContext(getContext());
    sslSocketFactory = new ConfigurableSSLSocketFactory(parameters,
            sslContext.getSocketFactory());
  }

  /**
   * Gets an {@link javax.net.SocketFactory} that produces SSL sockets using an
   * {@link javax.net.ssl.SSLContext} that is derived from the appender's configuration.
   *
   * @return socket factory
   */
  @Override
  protected SocketFactory getSocketFactory() {
    return sslSocketFactory;
  }


}
