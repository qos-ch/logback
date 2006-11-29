package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.core.spi.FilterReply;

/**
 * This class allows output for a given MDC value.
 * 
 * When the given value is identified by this TubroFilter, 
 * the reply is based on the OnMatch option.
 * The information is taken from the MDC. For this TurboFilter to work,
 * one must set the key that will be used to 
 * access the information in the MDC.
 * 
 * To allow output for the value, set the OnMatch option
 * to ACCEPT. To disable output for the given value, set
 * the OnMatch option to DENY.
 * 
 * By default, values of the OnMatch and OnMisMatch
 * options are NEUTRAL.
 * 
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class MDCFilter extends MatchingFilter {

  String MDCKey;
  String value;
  
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    if (MDCKey == null) {
      return FilterReply.NEUTRAL;
    }
    
    String value = MDC.get(MDCKey);
    if (this.value.equals(value)) {
      return onMatch;
    }
    return onMismatch;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public void setMDCKey(String MDCKey) {
    this.MDCKey = MDCKey;
  }

}
