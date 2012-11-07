package ch.qos.logback.core.status;

import java.io.PrintStream;

/**
 * Print all new incoming status messages on the error console (System.err).
 *
 * @author Ceki G&uuml;c&uuml;
 * @since 1.0.8
 */
public class OnErrorConsoleStatusListener extends OnPrintStreamStatusListenerBase {

  @Override
  protected PrintStream getPrintStream() {
    return System.err;
  }
}
