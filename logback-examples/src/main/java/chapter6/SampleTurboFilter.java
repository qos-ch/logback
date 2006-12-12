package chapter6;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class SampleTurboFilter extends TurboFilter {

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level,
      String format, Object[] params, Throwable t) {
    if ((MarkerFactory.getMarker("sample").equals(marker))) {
      return FilterReply.ACCEPT;
    } else {
      return FilterReply.NEUTRAL;
    }
  }
}
