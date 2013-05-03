/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
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

/**
 * A {@link ClientVisitor} with instrumentation for unit testing.
 *
 * @author Carl Harris
 */
public class MockClientVisitor implements ClientVisitor<WaitingClient> {

  private WaitingClient lastVisited;
  
  /**
   * {@inheritDoc}
   */
  public void visit(WaitingClient client) {
    lastVisited = client;
  }

  public WaitingClient getLastVisited() {
    return lastVisited;
  }

}
