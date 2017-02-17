package ch.qos.logback.core.net.mock;

import java.net.InetAddress;

import ch.qos.logback.core.net.AbstractSocketAppender;
import ch.qos.logback.core.net.DefaultSocketConnector;
import ch.qos.logback.core.net.ObjectWriterFactory;
import ch.qos.logback.core.net.QueueFactory;
import ch.qos.logback.core.net.SocketConnector;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class MockSocketAppender extends AbstractSocketAppender<String> {

    private PreSerializationTransformer<String> preSerializationTransformer;
    //private SocketConnector socketConnector;

    public MockSocketAppender(PreSerializationTransformer<String> preSerializationTransformer, SocketConnector socketConnector) {
        super();
        this.preSerializationTransformer = preSerializationTransformer;
        //this.socketConnector = socketConnector;
    }

    public MockSocketAppender(PreSerializationTransformer<String> preSerializationTransformer, QueueFactory queueFactory,
                    ObjectWriterFactory objectWriterFactory, SocketConnector socketConnector) {
        super(queueFactory, objectWriterFactory);
        this.preSerializationTransformer = preSerializationTransformer;
       // this.socketConnector = socketConnector;
    }

    @Override
    protected void postProcessEvent(String event) {
    }

    @Override
    protected PreSerializationTransformer<String> getPST() {
        return preSerializationTransformer;
    }

    @Override
    protected SocketConnector newConnector(InetAddress address, int port, long initialDelay, long retryDelay) {
        return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
    }
    
    public SocketConnector getSocketConnector() {
        return super.connector; 
    }
}