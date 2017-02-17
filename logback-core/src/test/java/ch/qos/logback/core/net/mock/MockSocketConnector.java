package ch.qos.logback.core.net.mock;

import java.net.Socket;

import javax.net.SocketFactory;

import ch.qos.logback.core.net.SocketConnector;

/**
 * A {@link SocketConnector} with instrumentation for unit testing.
 */
public class MockSocketConnector implements SocketConnector {

    private final Socket socket;

    public MockSocketConnector(Socket socket) {
        this.socket = socket;
    }

    public Socket call() throws InterruptedException {
        return socket;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    }

    public void setSocketFactory(SocketFactory socketFactory) {
    }
}