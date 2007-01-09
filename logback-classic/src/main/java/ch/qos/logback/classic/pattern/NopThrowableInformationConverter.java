package ch.qos.logback.classic.pattern;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreGlobal;



/**
 * Always returns an empty string.
 * <p>
 * This converter is useful to pretend that the converter chain for
 * PatternLayout actually handles exceptions, when in fact it does not.
 * By adding %nopex to the conversion pattern, the user can bypass
 * the automatic addition of %ex conversion pattern for patterns 
 * which do not contain a converter handling exceptions.
 * 
 * <p>Users can ignore the existence of this converter, unless they
 * want to suppress the automatic printing of exceptions by 
 * {@link PatternLayout}.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NopThrowableInformationConverter extends ThrowableHandlingConverter {

  public String convert(LoggingEvent event) {
    return CoreGlobal.EMPTY_STRING;
  }
 
}
