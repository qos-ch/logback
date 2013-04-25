package ch.qos.logback.core.sift;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Created with IntelliJ IDEA.
 * User: ceki
 * Date: 25.04.13
 * Time: 19:00
 * To change this template use File | Settings | File Templates.
 */
public interface AppenderFactory<E> {
  Appender<E> buildAppender(Context context, String discriminatingValue) throws JoranException;
}
