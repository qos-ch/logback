package chapter6;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

public class SampleTurboFilter extends TurboFilter {

  String marker;
  Marker acceptedMarker;

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level,
      String format, Object[] params, Throwable t) {

    if ((acceptedMarker.equals(marker))) {
      return FilterReply.ACCEPT;
    } else {
      return FilterReply.NEUTRAL;
    }
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(String markerStr) {
    this.marker = markerStr;
  }

  @Override
  public void start() {
    if (marker != null && marker.trim().length() > 0) {
      acceptedMarker = MarkerFactory.getMarker(marker);
      super.start(); 
    }
  }
}
