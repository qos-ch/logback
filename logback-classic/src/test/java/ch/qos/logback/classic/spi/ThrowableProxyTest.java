package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThrowableProxyTest {

  StringWriter sw = new StringWriter();
  PrintWriter pw = new PrintWriter(sw);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  public void verify(Throwable t) {
    t.printStackTrace(pw);

    IThrowableProxy tp = new ThrowableProxy(t);
    
    String result = ThrowableProxyUtil.asString(tp);
    result = result.replace("common frames omitted", "more");
        
    String expected = sw.toString();
    
    System.out.println("========expected");
    System.out.println(expected);

    System.out.println("========result");
    System.out.println(result);

    assertEquals(expected, result);
  }

  @Test
  public void smoke() {
    Exception e = new Exception("smoke");
    verify(e);
  }

  @Test
  public void nested() {
    Exception w = null;
    try {
      someMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
  }

  @Test
  public void multiNested() {
    Exception w = null;
    try {
      someOtherMethod();
    } catch (Exception e) {
      w = new Exception("wrapping", e);
    }
    verify(w);
  }

  void someMethod() throws Exception {
    throw new Exception("someMethod");
  }

  void someOtherMethod() throws Exception {
    try {
      someMethod();
    } catch (Exception e) {
      throw new Exception("someOtherMethod", e);
    }
  }
}
