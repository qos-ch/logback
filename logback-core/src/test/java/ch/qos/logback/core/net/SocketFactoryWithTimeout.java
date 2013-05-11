package ch.qos.logback.core.net;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketFactoryWithTimeout extends SocketFactory {

  final int timeout;

  public SocketFactoryWithTimeout(int timeout) {
    this.timeout = timeout;
  }

  @Override
  public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Socket createSocket(String s, int i, InetAddress inetAddress, int i2) throws IOException, UnknownHostException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Socket createSocket(InetAddress inetAddress, int port) throws IOException {
    Socket socket = new Socket();
    SocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
    socket.connect(socketAddress, timeout);
    return socket;
  }

  @Override
  public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException {
    throw new UnsupportedOperationException();
  }
}
