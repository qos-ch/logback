package ch.qos.logback.classic.net.mock;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class MockInitialContextFactory implements InitialContextFactory {
  static MockInitialContext mic;

  static {
    try {
      mic = new MockInitialContext();
    } catch (NamingException e) {
      e.printStackTrace();
    }

  }

  public Context getInitialContext(Hashtable<?, ?> environment)
      throws NamingException {
    return mic;
  }
  
  public static MockInitialContext getContext() {
    return mic;
  }

}
