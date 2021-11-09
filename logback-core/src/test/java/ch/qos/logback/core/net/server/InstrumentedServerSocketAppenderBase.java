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
package ch.qos.logback.core.net.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;

import javax.net.ServerSocketFactory;

import ch.qos.logback.core.spi.PreSerializationTransformer;

/**
 * A {@link AbstractServerSocketAppender} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class InstrumentedServerSocketAppenderBase extends AbstractServerSocketAppender<Serializable> {

	private final ServerSocket serverSocket;
	private final ServerListener<RemoteReceiverClient> listener;
	private final ServerRunner<RemoteReceiverClient> runner;

	@SuppressWarnings("rawtypes")
	private ServerListener lastListener;

	public InstrumentedServerSocketAppenderBase(final ServerSocket serverSocket) {
		this(serverSocket, new RemoteReceiverServerListener(serverSocket), null);
	}

	public InstrumentedServerSocketAppenderBase(final ServerSocket serverSocket, final ServerListener<RemoteReceiverClient> listener,
			final ServerRunner<RemoteReceiverClient> runner) {
		this.serverSocket = serverSocket;
		this.listener = listener;
		this.runner = runner;
	}

	@Override
	protected void postProcessEvent(final Serializable event) {
	}

	@Override
	protected PreSerializationTransformer<Serializable> getPST() {
		return event -> event;
	}

	@Override
	protected ServerSocketFactory getServerSocketFactory() throws Exception {
		return new ServerSocketFactory() {

			@Override
			public ServerSocket createServerSocket(final int port) throws IOException {
				return serverSocket;
			}

			@Override
			public ServerSocket createServerSocket(final int port, final int backlog) throws IOException {
				return serverSocket;
			}

			@Override
			public ServerSocket createServerSocket(final int port, final int backlog, final InetAddress ifAddress) throws IOException {
				return serverSocket;
			}
		};
	}

	@Override
	protected ServerRunner<RemoteReceiverClient> createServerRunner(final ServerListener<RemoteReceiverClient> listener, final Executor executor) {
		lastListener = listener;
		return runner != null ? runner : super.createServerRunner(listener, executor);
	}

	@Override
	protected ServerListener<RemoteReceiverClient> createServerListener(final ServerSocket socket) {
		return listener;
	}

	@SuppressWarnings("rawtypes")
	public ServerListener getLastListener() {
		return lastListener;
	}

}
