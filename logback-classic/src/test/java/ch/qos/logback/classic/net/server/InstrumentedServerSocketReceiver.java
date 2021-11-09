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
package ch.qos.logback.classic.net.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executor;

import javax.net.ServerSocketFactory;

import ch.qos.logback.core.net.server.ServerListener;
import ch.qos.logback.core.net.server.ServerRunner;

/**
 * A {@link ServerSocketReceiver} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class InstrumentedServerSocketReceiver extends ServerSocketReceiver {

	private final ServerSocket serverSocket;
	private final ServerListener<RemoteAppenderClient> listener;
	private final ServerRunner<RemoteAppenderClient> runner;

	@SuppressWarnings("rawtypes")
	private ServerListener lastListener;

	public InstrumentedServerSocketReceiver(final ServerSocket serverSocket) {
		this(serverSocket, new RemoteAppenderServerListener(serverSocket), null);
	}

	public InstrumentedServerSocketReceiver(final ServerSocket serverSocket, final ServerListener<RemoteAppenderClient> listener, final ServerRunner<RemoteAppenderClient> runner) {
		this.serverSocket = serverSocket;
		this.listener = listener;
		this.runner = runner;
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

	@SuppressWarnings("rawtypes")
	@Override
	protected ServerRunner createServerRunner(final ServerListener<RemoteAppenderClient> listener, final Executor executor) {
		lastListener = listener;
		return runner != null ? runner : super.createServerRunner(listener, executor);
	}

	@Override
	protected ServerListener<RemoteAppenderClient> createServerListener(final ServerSocket socket) {
		return listener;
	}

	@SuppressWarnings("rawtypes")
	public ServerListener getLastListener() {
		return lastListener;
	}

}