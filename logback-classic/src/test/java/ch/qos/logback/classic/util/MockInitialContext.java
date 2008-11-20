package ch.qos.logback.classic.util;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MockInitialContext extends InitialContext {

  public Map<String, Object> map = new HashMap<String, Object>();

  public MockInitialContext() throws NamingException {
    super();
  }

  @Override
  public Object lookup(String name) throws NamingException {
    if (name == null) {
      return null;
    }

    return map.get(name);
  }

}
