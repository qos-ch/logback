package ch.qos.logback.core.status;

/**
 * A no-operation (nop) StatusListener
 *
 * @author Ceki G&uuml;c&uuml;
 * @since 1.0.8
 */
public class NopStatusListener implements StatusListener {

  public void addStatusEvent(Status status) {
   // nothing to do
  }
}
