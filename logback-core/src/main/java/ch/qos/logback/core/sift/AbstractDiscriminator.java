package ch.qos.logback.core.sift;

import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Base implementation of {@link Discriminator} that provides basic lifecycle management
 *
 * @author Tomasz Nurkiewicz
 * @since 3/29/13, 3:28 PM
 */
public abstract class AbstractDiscriminator<E> extends ContextAwareBase implements Discriminator<E> {

  protected boolean started;

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }
}
