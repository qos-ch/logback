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
package ch.qos.logback.core.net.server.test;

import java.io.IOException;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.net.server.Client;
import ch.qos.logback.core.net.server.ClientVisitor;
import ch.qos.logback.core.net.server.ServerRunner;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A mock {@link ServerRunner} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockServerRunner<T extends Client> extends ContextAwareBase implements ServerRunner<T> {

	private IOException stopException;
	private int startCount;
	private boolean contextInjected;

	@Override
	public void setContext(final Context context) {
		contextInjected = true;
		super.setContext(context);
	}

	@Override
	public void run() {
		startCount++;
	}

	@Override
	public void stop() throws IOException {
		if (stopException != null) {
			throw stopException;
		}
		startCount--;
	}

	@Override
	public boolean isRunning() {
		return startCount > 0;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void accept(final ClientVisitor visitor) {
		throw new UnsupportedOperationException();
	}

	public int getStartCount() {
		return startCount;
	}

	public boolean isContextInjected() {
		return contextInjected;
	}

	public void setStopException(final IOException stopException) {
		this.stopException = stopException;
	}

}
