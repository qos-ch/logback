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
package ch.qos.logback.core.net;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import ch.qos.logback.core.net.mock.MockContext;
import ch.qos.logback.core.spi.PreSerializationTransformer;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.ExecutorServiceUtil;

/**
 * Unit tests for {@link AbstractSocketAppender}.
 *
 * @author Carl Harris
 * @author Sebastian Gr&ouml;bler
 */
public class AbstractSocketAppenderTest {

    /**
     * Timeout used for all blocking operations in multi-threading contexts.
     */
    private static final int TIMEOUT = 1000;

    private ScheduledExecutorService executorService = spy((ScheduledExecutorService) ExecutorServiceUtil.newScheduledExecutorService());
    private MockContext mockContext = new MockContext(executorService);
    private PreSerializationTransformer<String> preSerializationTransformer = spy(new StringPreSerializationTransformer());
    private Socket socket = mock(Socket.class);
    private SocketConnector socketConnector = mock(SocketConnector.class);
    private AutoFlushingObjectWriter objectWriter = mock(AutoFlushingObjectWriter.class);
    private ObjectWriterFactory objectWriterFactory = mock(ObjectWriterFactory.class);
    private LinkedBlockingDeque<String> deque = spy(new LinkedBlockingDeque<String>(1));
    private QueueFactory queueFactory = mock(QueueFactory.class);
    private InstrumentedSocketAppender appender = spy(new InstrumentedSocketAppender(preSerializationTransformer, queueFactory, objectWriterFactory,
                    socketConnector));

    @Before
    public void setupValidAppenderWithMockDependencies() throws Exception {

        doReturn(objectWriter).when(objectWriterFactory).newAutoFlushingObjectWriter(any(OutputStream.class));
        doReturn(deque).when(queueFactory).<String> newLinkedBlockingDeque(anyInt());

        appender.setContext(mockContext);
        appender.setRemoteHost("localhost");
    }

    @After
    public void tearDown() throws Exception {
        appender.stop();
        assertFalse(appender.isStarted());

        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void failsToStartWithoutValidPort() throws Exception {

        // given
        appender.setPort(-1);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        verify(appender).addError(contains("port"));
    }

    @Test
    public void failsToStartWithoutValidRemoteHost() throws Exception {

        // given
        appender.setRemoteHost(null);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        verify(appender).addError(contains("remote host"));
    }

    @Test
    public void failsToStartWithNegativeQueueSize() throws Exception {

        // given
        appender.setQueueSize(-1);

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        verify(appender).addError(contains("Queue size must be greater than zero"));
    }

    @Test
    public void failsToStartWithUnresolvableRemoteHost() throws Exception {

        // given
        appender.setRemoteHost("NOT.A.VALID.REMOTE.HOST.NAME");

        // when
        appender.start();

        // then
        assertFalse(appender.isStarted());
        verify(appender).addError(contains("unknown host"));
    }

    @Test
    public void startsButOutputsWarningWhenQueueSizeIsZero() throws Exception {

        // given
        appender.setQueueSize(0);

        // when
        appender.start();

        // then
        assertTrue(appender.isStarted());
        verify(appender).addWarn("Queue size of zero is deprecated, use a size of one to indicate synchronous processing");
    }

    @Test
    public void startsWithValidParameters() throws Exception {

        // when
        appender.start();

        // then
        assertTrue(appender.isStarted());
    }

    @Test
    public void createsSocketConnectorWithConfiguredParameters() throws Exception {

        // given
        appender.setReconnectionDelay(new Duration(42));
        appender.setRemoteHost("localhost");
        appender.setPort(21);

        // when
        appender.start();

        // then
        verify(appender, timeout(TIMEOUT)).newConnector(InetAddress.getByName("localhost"), 21, 0, 42);
    }

    @Test
    public void addsInfoMessageWhenSocketConnectionWasEstablished() throws Exception {

        // when
        mockOneSuccessfulSocketConnection();
        appender.start();

        // then
        verify(appender, timeout(TIMEOUT)).addInfo(contains("connection established"));
    }

    @Test
    public void addsInfoMessageWhenSocketConnectionFailed() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(objectWriterFactory).newAutoFlushingObjectWriter(any(OutputStream.class));
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(appender, timeout(TIMEOUT).atLeastOnce()).addInfo(contains("connection failed"));
    }

    @Test
    public void closesSocketOnException() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(objectWriterFactory).newAutoFlushingObjectWriter(any(OutputStream.class));
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(socket, timeout(TIMEOUT).atLeastOnce()).close();
    }

    @Test
    public void addsInfoMessageWhenSocketConnectionClosed() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(objectWriterFactory).newAutoFlushingObjectWriter(any(OutputStream.class));
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(appender, timeout(TIMEOUT).atLeastOnce()).addInfo(contains("connection closed"));
    }

    @Test
    public void shutsDownOnInterruptWhileWaitingForEvent() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new InterruptedException()).when(deque).takeFirst();

        // when
        appender.start();

        // then
        verify(deque, timeout(TIMEOUT)).takeFirst();
    }

    @Test
    public void shutsDownOnInterruptWhileWaitingForSocketConnection() throws Exception {

        // given
        doThrow(new InterruptedException()).when(socketConnector).call();

        // when
        appender.start();

        // then
        verify(socketConnector, timeout(TIMEOUT)).call();
    }

    @Test
    public void addsInfoMessageWhenShuttingDownDueToInterrupt() throws Exception {

        // given
        doThrow(new InterruptedException()).when(socketConnector).call();

        // when
        appender.start();

        // then
        verify(appender, timeout(TIMEOUT)).addInfo(contains("shutting down"));
    }

    @Test
    public void offersEventsToTheEndOfTheDeque() throws Exception {

        // given
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(deque).offer(eq("some event"), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void doesNotQueueAnyEventsWhenStopped() throws Exception {

        // given
        appender.start();
        appender.stop();

        // when
        appender.append("some event");

        // then
        verifyZeroInteractions(deque);
    }

    @Test
    public void addsInfoMessageWhenEventCouldNotBeQueuedInConfiguredTimeoutDueToQueueSizeLimitation() throws Exception {

        // given
        long eventDelayLimit = 42;
        doReturn(false).when(deque).offer("some event", eventDelayLimit, TimeUnit.MILLISECONDS);
        appender.setEventDelayLimit(new Duration(eventDelayLimit));
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(appender).addInfo("Dropping event due to timeout limit of [" + eventDelayLimit + " milliseconds] being exceeded");
    }

    @Test
    public void takesEventsFromTheFrontOfTheDeque() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        appender.start();
        awaitStartOfEventDispatching();

        // when
        appender.append("some event");

        // then
        verify(deque, timeout(TIMEOUT).atLeastOnce()).takeFirst();
    }

    @Test
    public void reAddsEventAtTheFrontOfTheDequeWhenTransmissionFails() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(objectWriter).write(anyObject());
        appender.start();
        awaitStartOfEventDispatching();

        // when
        appender.append("some event");

        // then
        verify(deque, timeout(TIMEOUT).atLeastOnce()).offerFirst("some event");
    }

    @Test
    public void addsErrorMessageWhenAppendingIsInterruptedWhileWaitingForTheQueueToAcceptTheEvent() throws Exception {

        // given
        final InterruptedException interruptedException = new InterruptedException();
        doThrow(interruptedException).when(deque).offer(eq("some event"), anyLong(), any(TimeUnit.class));
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(appender).addError("Interrupted while appending event to SocketAppender", interruptedException);
    }

    @Test
    public void postProcessesEventsBeforeTransformingItToASerializable() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        appender.start();
        awaitStartOfEventDispatching();

        // when
        appender.append("some event");
        awaitAtLeastOneEventToBeDispatched();

        // then
        InOrder inOrder = inOrder(appender, preSerializationTransformer);
        inOrder.verify(appender).postProcessEvent("some event");
        inOrder.verify(preSerializationTransformer).transform("some event");
    }

    @Test
    public void writesSerializedEventToStream() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        when(preSerializationTransformer.transform("some event")).thenReturn("some serialized event");
        appender.start();
        awaitStartOfEventDispatching();

        // when
        appender.append("some event");

        // then
        verify(objectWriter, timeout(TIMEOUT)).write("some serialized event");
    }

    @Test
    public void addsInfoMessageWhenEventIsBeingDroppedBecauseOfConnectionProblemAndDequeCapacityLimitReached() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(objectWriter).write(anyObject());
        doReturn(false).when(deque).offerFirst("some event");
        appender.start();
        awaitStartOfEventDispatching();
        reset(appender);

        // when
        appender.append("some event");

        // then
        verify(appender, timeout(TIMEOUT)).addInfo("Dropping event due to socket connection error and maxed out deque capacity");
    }

    @Test
    public void reEstablishesSocketConnectionOnConnectionDropWhenWritingEvent() throws Exception {

        // given
        mockTwoSuccessfulSocketConnections();
        doThrow(new IOException()).when(objectWriter).write(anyObject());
        appender.start();
        awaitStartOfEventDispatching();

        // when
        appender.append("some event");

        // then
        verify(objectWriterFactory, timeout(TIMEOUT).atLeast(2)).newAutoFlushingObjectWriter(any(OutputStream.class));
    }

    @Test
    public void triesToReEstablishSocketConnectionIfItFailed() throws Exception {

        // given
        mockOneSuccessfulSocketConnection();
        doThrow(new IOException()).when(socket).getOutputStream();
        appender.start();

        // when
        appender.append("some event");

        // then
        verify(socketConnector, timeout(TIMEOUT).atLeast(2)).call();
    }

    @Test
    public void usesConfiguredAcceptConnectionTimeoutAndResetsSocketTimeoutAfterSuccessfulConnection() throws Exception {

        // when
        mockOneSuccessfulSocketConnection();
        appender.setAcceptConnectionTimeout(42);
        appender.start();
        awaitStartOfEventDispatching();

        // then
        InOrder inOrder = inOrder(socket);
        inOrder.verify(socket).setSoTimeout(42);
        inOrder.verify(socket).setSoTimeout(0);
    }

    private void awaitAtLeastOneEventToBeDispatched() throws IOException {
        verify(objectWriter, timeout(TIMEOUT)).write(anyString());
    }

    private void awaitStartOfEventDispatching() throws InterruptedException {
        verify(deque, timeout(TIMEOUT)).takeFirst();
    }

    private void mockOneSuccessfulSocketConnection() throws InterruptedException {
        doReturn(socket).doReturn(null).when(socketConnector).call();
    }

    private void mockTwoSuccessfulSocketConnections() throws InterruptedException {
        doReturn(socket).doReturn(socket).doReturn(null).when(socketConnector).call();
    }

    private static class InstrumentedSocketAppender extends AbstractSocketAppender<String> {

        private PreSerializationTransformer<String> preSerializationTransformer;
        private SocketConnector socketConnector;

        public InstrumentedSocketAppender(PreSerializationTransformer<String> preSerializationTransformer, QueueFactory queueFactory,
                        ObjectWriterFactory objectWriterFactory, SocketConnector socketConnector) {
            super(queueFactory, objectWriterFactory);
            this.preSerializationTransformer = preSerializationTransformer;
            this.socketConnector = socketConnector;
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
            return socketConnector;
        }
    }

    private static class StringPreSerializationTransformer implements PreSerializationTransformer<String> {

        @Override
        public Serializable transform(String event) {
            return event;
        }
    }
}
