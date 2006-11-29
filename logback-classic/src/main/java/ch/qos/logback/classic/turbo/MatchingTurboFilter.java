package ch.qos.logback.classic.turbo;

import ch.qos.logback.core.spi.FilterReply;

/**
 * An abstract class containing support for {@link #onMatch} on {@link #onMismatch} 
 * attributes, shared by many but not all tubo filters.
 *  
 * @author Ceki Gulcu
 */
public abstract class MatchingTurboFilter extends TurboFilter {

  protected FilterReply onMatch = FilterReply.NEUTRAL;
  protected FilterReply onMismatch = FilterReply.NEUTRAL;
    
  final public void setOnMatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMatch = FilterReply.DENY;
    }
  }

  final public void setOnMismatch(String action) {
    if ("NEUTRAL".equals(action)) {
      onMismatch = FilterReply.NEUTRAL;
    } else if ("ACCEPT".equals(action)) {
      onMismatch = FilterReply.ACCEPT;
    } else if ("DENY".equals(action)) {
      onMismatch = FilterReply.DENY;
    }
  }
}
