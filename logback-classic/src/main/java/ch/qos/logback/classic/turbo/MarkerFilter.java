package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Checks whether the marker in the event matches the marker specified by the 
 * user.
 */
public class MarkerFilter extends MatchingFilter {

  Marker marker2Match;

  @Override
  public void start() {
    if(marker2Match != null) {
      super.start();
    } else {
      addError("The marker property must be set for ["+getName()+"]");
    }
  }
  
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    if(!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    
    if(marker == null) {
      return onMismatch;
    } 
    
    if(marker2Match.contains(marker)) {
      return onMatch;
    } else {
      return onMismatch;
    }
  }

  /**
   * The marker to match in the event.
   * 
   * @param marker2Match
   */
  public void setMarker(String markerStr) {
    if(markerStr != null) {
      this.marker2Match = MarkerFactory.getMarker(markerStr);
    }
  }
}
