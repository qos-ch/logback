package ch.qos.logback.classic.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A simple utility class to create and use a JNDI Context.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */

public class JNDIUtil {
  
  public static Context getInitialContext() throws NamingException {
    return new InitialContext();
  }

  public static String lookup(Context ctx, String name) {
    if (ctx == null) {
      return null;
    }
    try {
      return (String) ctx.lookup(name);
    } catch (NamingException e) {
      return null;
    }
  }
}