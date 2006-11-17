package ch.qos.logback.classic.turbo;

import org.slf4j.Marker;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.core.spi.FilterReply;

/**
 * This class allows output for a given user.
 * 
 * When the given user is identified by this TubroFilter, 
 * the reply is based on the OnMatch option.
 * 
 * To allow output for a user, set the OnMatch option
 * to ACCEPT. To disable output for the given user, set
 * the OnMatch option to DENY.
 * 
 * By default, values of the OnMatch and OnMisMatch
 * options are NEUTRAL.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class UserTurboFilter extends TurboFilter {

  String user;
  
  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {

    String user = MDC.get(ClassicGlobal.USER_MDC_KEY);
    if (this.user.equals(user)) {
      return onMatch;
    }
    return onMismatch;
  }
  
  public void setUser(String user) {
    this.user = user;
  }

}
