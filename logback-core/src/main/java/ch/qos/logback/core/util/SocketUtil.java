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
package ch.qos.logback.core.util;

import java.io.IOException;
import java.net.Socket;

/**
 * Static utility methods for {@link Socket} objects.
 *
 * @author Carl Harris
 */
public class SocketUtil {

  /**
   * Closes a socket while suppressing any {@code IOException} that occurs.
   * @param socket the socket to close
   */
  public static void closeQuietly(Socket socket) {
    if (socket == null) return;
    try {
      socket.close();
    }
    catch (IOException ex) {
      assert true;  // avoid an empty catch
    }
  }

}
