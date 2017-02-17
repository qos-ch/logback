package ch.qos.logback.core.net.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * A no-op {@link SocketFactory} to support unit testing.
 */
public class MockSocketFactory extends SocketFactory {

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        throw new UnsupportedOperationException();
    }

}