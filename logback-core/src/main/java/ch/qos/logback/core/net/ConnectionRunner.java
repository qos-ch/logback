package ch.qos.logback.core.net;


import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.Duration;

import javax.net.SocketFactory;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class ConnectionRunner extends ContextAwareBase implements SocketConnector.ExceptionHandler {

  /**
   * The default reconnection delay (30000 milliseconds or 30 seconds).
   */
  public static final int DEFAULT_RECONNECTION_DELAY = 30000;
  private long reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

  private Future<Socket> connectorTask;

  private String remoteHost;
  private int port;
  SocketFactory socketFactory;

  public ConnectionRunner(ContextAware contextAware, String remoteHost, int port, Duration reconnectionDuration) {
    super(contextAware);
    setContext(contextAware.getContext());
    this.remoteHost = remoteHost;
    this.port = port;
    if(reconnectionDuration != null) {
      reconnectionDelay = reconnectionDuration.getMilliseconds();
    }
  }

  public void stop() {
    addInfo("Stopping ConnectionRunner");
    if (connectorTask != null)
      connectorTask.cancel(true);
  }

  /**
   * Returns the SocketFactory set by {@link #setSocketFactory(javax.net.SocketFactory)}. If no factory is set,
   * return the default {@link SocketFactory} for the platform.
   * <p/>
   * Subclasses may override to provide a custom socket factory.
   */
  protected SocketFactory getSocketFactory() {
    if(socketFactory == null) {
      socketFactory =  SocketFactory.getDefault();
    }
    return socketFactory;
  }

  protected void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  public Socket connect() throws InterruptedException {
    InetAddress address = resolve(remoteHost);
    if(address == null) return null;
    SocketConnector connector = createConnector(address, port, reconnectionDelay);
    connectorTask = activateConnector(connector);
    if (connectorTask == null)
      return null;
    return waitForConnectorToReturnASocket();
  }

  private InetAddress resolve(String remoteHost) {
    try {
      return InetAddress.getByName(remoteHost);
    } catch (UnknownHostException ex) {
      addError("unknown host: " + remoteHost, ex);
      return null;
    }
  }

  private SocketConnector createConnector(InetAddress address, int port,
                                          long retryDelay) {
    SocketConnector connector = newConnector(address, port, retryDelay);
    connector.setExceptionHandler(this);
    connector.setSocketFactory(getSocketFactory());
    return connector;
  }

  /**
   * Creates a new {@link SocketConnector}.
   * <p/>
   * The default implementation creates an instance of {@link DefaultSocketConnector}.
   * A subclass may override to provide a different {@link SocketConnector}
   * implementation.
   *
   * @param address      target remote address
   * @param port         target remote port
   * @param retryDelay   delay before a reconnection attempt
   * @return socket connector
   */
  protected SocketConnector newConnector(InetAddress address, int port,  long retryDelay) {
    int initialDelay = 0;
    return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
  }

  private Future<Socket> activateConnector(SocketConnector connector) {
    try {
      return getContext().getExecutorService().submit(connector);
    } catch (RejectedExecutionException ex) {
      return null;
    }
  }

  private Socket waitForConnectorToReturnASocket() throws InterruptedException {
    try {
      Socket s = connectorTask.get();
      connectorTask = null;
      return s;
    } catch (ExecutionException e) {
      return null;
    }
  }


  String getPeerId() {
    return remoteHost + ":" + port;
  }

  public void connectionFailed(SocketConnector connector, Exception ex) {
    if (ex instanceof InterruptedException) {
      addInfo("connector interrupted");
    } else if (ex instanceof ConnectException) {
      addInfo(getPeerId() + " connection refused");
    } else {
      addInfo(getPeerId(), ex);
    }
  }
}
