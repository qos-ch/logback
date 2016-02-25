/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
// Contributors: Dan MacDonald <dan@redknee.com>
package ch.qos.logback.core.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.util.CloseUtil;
import ch.qos.logback.core.util.Duration;

/**
 * An abstract base for module specific {@code SocketAppender}
 * implementations in other logback modules.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 */

public abstract class AbstractSocketAppender<E> extends AppenderBase<E> implements SocketConnector.ExceptionHandler {

    /**
     * The default port number of remote logging server (4560).
     */
    public static final int DEFAULT_PORT = 4560;

    /**
     * The default reconnection delay (30000 milliseconds or 30 seconds).
     */
    public static final int DEFAULT_RECONNECTION_DELAY = 30000;

    /**
     * Default size of the deque used to hold logging events that are destined
     * for the remote peer.
     */
    public static final int DEFAULT_QUEUE_SIZE = 128;

    /**
     * Default timeout when waiting for the remote server to accept our
     * connection.
     */
    private static final int DEFAULT_ACCEPT_CONNECTION_DELAY = 5000;

    /**
     * Default timeout for how long to wait when inserting an event into
     * the BlockingQueue.
     */
    private static final int DEFAULT_EVENT_DELAY_TIMEOUT = 100;

    private final ObjectWriterFactory objectWriterFactory;
    private final QueueFactory queueFactory;

    private String remoteHost;
    private int port = DEFAULT_PORT;
    private InetAddress address;
    private Duration reconnectionDelay = new Duration(DEFAULT_RECONNECTION_DELAY);
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private int acceptConnectionTimeout = DEFAULT_ACCEPT_CONNECTION_DELAY;
    private Duration eventDelayLimit = new Duration(DEFAULT_EVENT_DELAY_TIMEOUT);

    private BlockingDeque<E> deque;
    private String peerId;
    private SocketConnector connector;
    private Future<?> task;

    private volatile Socket socket;

    /**
     * Constructs a new appender.
     */
    protected AbstractSocketAppender() {
        this(new QueueFactory(), new ObjectWriterFactory());
    }

    /**
     * Constructs a new appender using the given {@link QueueFactory} and {@link ObjectWriterFactory}.
     */
    AbstractSocketAppender(QueueFactory queueFactory, ObjectWriterFactory objectWriterFactory) {
        this.objectWriterFactory = objectWriterFactory;
        this.queueFactory = queueFactory;
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
        if (isStarted())
            return;
        int errorCount = 0;
        if (port <= 0) {
            errorCount++;
            addError("No port was configured for appender" + name + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");
        }

        if (remoteHost == null) {
            errorCount++;
            addError("No remote host was configured for appender" + name
                            + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
        }

        if (queueSize == 0) {
            addWarn("Queue size of zero is deprecated, use a size of one to indicate synchronous processing");
        }

        if (queueSize < 0) {
            errorCount++;
            addError("Queue size must be greater than zero");
        }

        if (errorCount == 0) {
            try {
                address = InetAddress.getByName(remoteHost);
            } catch (UnknownHostException ex) {
                addError("unknown host: " + remoteHost);
                errorCount++;
            }
        }

        if (errorCount == 0) {
            deque = queueFactory.newLinkedBlockingDeque(queueSize);
            peerId = "remote peer " + remoteHost + ":" + port + ": ";
            connector = createConnector(address, port, 0, reconnectionDelay.getMilliseconds());
            task = getContext().getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    connectSocketAndDispatchEvents();
                }
            });
            super.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (!isStarted())
            return;
        CloseUtil.closeQuietly(socket);
        task.cancel(true);
        super.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append(E event) {
        if (event == null || !isStarted())
            return;

        try {
            final boolean inserted = deque.offer(event, eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
            if (!inserted) {
                addInfo("Dropping event due to timeout limit of [" + eventDelayLimit + "] being exceeded");
            }
        } catch (InterruptedException e) {
            addError("Interrupted while appending event to SocketAppender", e);
        }
    }

    private void connectSocketAndDispatchEvents() {
        try {
            while (socketConnectionCouldBeEstablished()) {
                try {
                    ObjectWriter objectWriter = createObjectWriterForSocket();
                    addInfo(peerId + "connection established");
                    dispatchEvents(objectWriter);
                } catch (IOException ex) {
                    addInfo(peerId + "connection failed: " + ex);
                } finally {
                    CloseUtil.closeQuietly(socket);
                    socket = null;
                    addInfo(peerId + "connection closed");
                }
            }
        } catch (InterruptedException ex) {
            assert true; // ok... we'll exit now
        }
        addInfo("shutting down");
    }

    private boolean socketConnectionCouldBeEstablished() throws InterruptedException {
        return (socket = connector.call()) != null;
    }

    private ObjectWriter createObjectWriterForSocket() throws IOException {
        socket.setSoTimeout(acceptConnectionTimeout);
        ObjectWriter objectWriter = objectWriterFactory.newAutoFlushingObjectWriter(socket.getOutputStream());
        socket.setSoTimeout(0);
        return objectWriter;
    }

    private SocketConnector createConnector(InetAddress address, int port, int initialDelay, long retryDelay) {
        SocketConnector connector = newConnector(address, port, initialDelay, retryDelay);
        connector.setExceptionHandler(this);
        connector.setSocketFactory(getSocketFactory());
        return connector;
    }

    private void dispatchEvents(ObjectWriter objectWriter) throws InterruptedException, IOException {
        while (true) {
            E event = deque.takeFirst();
            postProcessEvent(event);
            Serializable serializableEvent = getPST().transform(event);
            try {
                objectWriter.write(serializableEvent);
            } catch (IOException e) {
                tryReAddingEventToFrontOfQueue(event);
                throw e;
            }
        }
    }

    private void tryReAddingEventToFrontOfQueue(E event) {
        final boolean wasInserted = deque.offerFirst(event);
        if (!wasInserted) {
            addInfo("Dropping event due to socket connection error and maxed out deque capacity");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connectionFailed(SocketConnector connector, Exception ex) {
        if (ex instanceof InterruptedException) {
            addInfo("connector interrupted");
        } else if (ex instanceof ConnectException) {
            addInfo(peerId + "connection refused");
        } else {
            addInfo(peerId + ex);
        }
    }

    /**
     * Creates a new {@link SocketConnector}.
     * <p>
     * The default implementation creates an instance of {@link DefaultSocketConnector}.
     * A subclass may override to provide a different {@link SocketConnector}
     * implementation.
     * 
     * @param address target remote address
     * @param port target remote port
     * @param initialDelay delay before the first connection attempt
     * @param retryDelay delay before a reconnection attempt
     * @return socket connector
     */
    protected SocketConnector newConnector(InetAddress address, int port, long initialDelay, long retryDelay) {
        return new DefaultSocketConnector(address, port, initialDelay, retryDelay);
    }

    /**
     * Gets the default {@link SocketFactory} for the platform.
     * <p>
     * Subclasses may override to provide a custom socket factory.
     */
    protected SocketFactory getSocketFactory() {
        return SocketFactory.getDefault();
    }

    /**
     * Post-processes an event before it is serialized for delivery to the
     * remote receiver.
     * @param event the event to post-process
     */
    protected abstract void postProcessEvent(E event);

    /**
     * Get the pre-serialization transformer that will be used to transform
     * each event into a Serializable object before delivery to the remote
     * receiver.
     * @return transformer object
     */
    protected abstract PreSerializationTransformer<E> getPST();

    /**
     * The <b>RemoteHost</b> property takes the name of of the host where a corresponding server is running.
     */
    public void setRemoteHost(String host) {
        remoteHost = host;
    }

    /**
     * Returns value of the <b>RemoteHost</b> property.
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * The <b>Port</b> property takes a positive integer representing the port
     * where the server is waiting for connections.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns value of the <b>Port</b> property.
     */
    public int getPort() {
        return port;
    }

    /**
     * The <b>reconnectionDelay</b> property takes a positive {@link Duration} value
     * representing the time to wait between each failed connection attempt
     * to the server. The default value of this option is to 30 seconds.
     *
     * <p>
     * Setting this option to zero turns off reconnection capability.
     */
    public void setReconnectionDelay(Duration delay) {
        this.reconnectionDelay = delay;
    }

    /**
     * Returns value of the <b>reconnectionDelay</b> property.
     */
    public Duration getReconnectionDelay() {
        return reconnectionDelay;
    }

    /**
     * The <b>queueSize</b> property takes a non-negative integer representing
     * the number of logging events to retain for delivery to the remote receiver.
     * When the deque size is zero, event delivery to the remote receiver is
     * synchronous.  When the deque size is greater than zero, the
     * {@link #append(Object)} method returns immediately after enqueing the
     * event, assuming that there is space available in the deque.  Using a
     * non-zero deque length can improve performance by eliminating delays
     * caused by transient network delays.
     * 
     * @param queueSize the deque size to set.
     */
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Returns the value of the <b>queueSize</b> property.
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * The <b>eventDelayLimit</b> takes a non-negative integer representing the
     * number of milliseconds to allow the appender to block if the underlying
     * BlockingQueue is full. Once this limit is reached, the event is dropped.
     *
     * @param eventDelayLimit the event delay limit
     */
    public void setEventDelayLimit(Duration eventDelayLimit) {
        this.eventDelayLimit = eventDelayLimit;
    }

    /**
     * Returns the value of the <b>eventDelayLimit</b> property.
     */
    public Duration getEventDelayLimit() {
        return eventDelayLimit;
    }

    /**
     * Sets the timeout that controls how long we'll wait for the remote
     * peer to accept our connection attempt.
     * <p>
     * This property is configurable primarily to support instrumentation
     * for unit testing.
     * 
     * @param acceptConnectionTimeout timeout value in milliseconds
     */
    void setAcceptConnectionTimeout(int acceptConnectionTimeout) {
        this.acceptConnectionTimeout = acceptConnectionTimeout;
    }

}
