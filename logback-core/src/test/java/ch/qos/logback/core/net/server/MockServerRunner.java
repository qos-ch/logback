/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2011, QOS.ch. All rights reserved.
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

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * A mock {@link ServerRunner} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockServerRunner extends ContextAwareBase implements ServerRunner {

  private IOException startException;
  private IOException stopException;
  private int startCount;
  private boolean contextInjected;
  
  @Override
  public void setContext(Context context) {
    contextInjected = true;    
    super.setContext(context);
  }

  public void start() throws IOException {
    if (startException != null) {
      throw startException;
    }
    startCount++;
  }

  public void stop() throws IOException {
    if (stopException != null) {
      throw stopException;
    }
    startCount--;
  }

  public boolean isStarted() {
    return startCount > 0;
  }

  public void accept(ClientVisitor visitor) {
    throw new UnsupportedOperationException();
  }

  public int getStartCount() {
    return startCount;
  }

  public boolean isContextInjected() {
    return contextInjected;
  }

  public void setStartException(IOException startException) {
    this.startException = startException;
  }

  public void setStopException(IOException stopException) {
    this.stopException = stopException;
  }
  
}
