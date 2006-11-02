package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

public class NOPTurboFilter extends TurboFilter {

  @Override
  public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format,
      final Object[] params, final Throwable t) {
   
    return FilterReply.NEUTRAL;
  }

}
