package ch.qos.logback.classic.turbo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.ClassicGlobal;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.MDC;
import ch.qos.logback.core.filter.Filter;

/**
 * This class allows output of debug level events to a certain list of users.
 * 
 * If the level passed as a parameter is of level DEBUG, then the "user" value
 * taken from the MDC is checked against the configured user list. When the user
 * belongs to the list, the request is accepted. Otherwise a NEUTRAL response
 * is sent, thus not influencing the filter chain.  
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class DebugUsersTurboFilter extends TurboFilter {

  List<String> userList = new ArrayList<String>(); 
  
  @Override
  public int decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    if (!level.equals(Level.DEBUG)) {
      return Filter.NEUTRAL;
    } 
    String user = MDC.get(ClassicGlobal.USER_MDC_KEY);
    if (user != null && userList.contains(user)) {
      return Filter.ACCEPT;
    }
    return Filter.NEUTRAL;
  }
  
  public void addUser(String user) {
    System.out.println("******* ADD USER CALLED");
    userList.add(user);
  }
  
  public List<String> getUsers() {
    return userList;
  }

}
