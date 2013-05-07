package ch.qos.logback.core.net;


import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

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
  private int reconnectionDelay = DEFAULT_RECONNECTION_DELAY;

  private Future<Socket> connectorTask;

  private String remoteHost;
  private int port;

  protected ConnectionRunner(Context context, String remoteHost, int port) {
    super(context);
    this.remoteHost = remoteHost;
    this.port = port;
  }


  void stop() {
    if (connectorTask != null)
      connectorTask.cancel(true);
  }

  private SocketConnector createConnector(InetAddress address, int port,
                                          int initialDelay, int retryDelay) {
    SocketConnector connector = newConnector(address, port, initialDelay,
            retryDelay);
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
   * @param initialDelay delay before the first connection attempt
   * @param retryDelay   delay before a reconnection attempt
   * @return socket connector
   */
  protected SocketConnector newConnector(InetAddress address,
                                         int port, int initialDelay, int retryDelay) {
    return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
  }

  /**
   * Gets the default {@link SocketFactory} for the platform.
   * <p/>
   * Subclasses may override to provide a custom socket factory.
   */
  protected SocketFactory getSocketFactory() {
    return SocketFactory.getDefault();
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


  /**
   * The <b>reconnectionDelay</b> property takes a positive integer representing
   * the number of milliseconds to wait between each failed connection attempt
   * to the server. The default value of this option is 30000 which corresponds
   * to 30 seconds.
   * <p/>
   * <p/>
   * Setting this option to zero turns off reconnection capability.
   */
  public void setReconnectionDelay(int delay) {
    this.reconnectionDelay = delay;
  }

  /**
   * Returns value of the <b>reconnectionDelay</b> property.
   */
  public int getReconnectionDelay() {
    return reconnectionDelay;
  }

  public Socket connect() throws InterruptedException {
    InetAddress address = resolve(remoteHost);
    SocketConnector connector = createConnector(address, port, 0,
            reconnectionDelay);
    connectorTask = activateConnector(connector);
    if (connectorTask == null)
      return null;
    return waitForConnectorToReturnASocket();
  }

  InetAddress resolve(String remoteHost) {
    try {
      return InetAddress.getByName(remoteHost);
    } catch (UnknownHostException ex) {
      addError("unknown host: " + remoteHost, ex);
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
