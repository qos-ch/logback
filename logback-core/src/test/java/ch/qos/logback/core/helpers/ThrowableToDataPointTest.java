package ch.qos.logback.core.helpers;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Layout;

public class ThrowableToDataPointTest {

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
    
    ThrowableDataPoint[] tdpArray = ThrowableToDataPointArray.convert(t);
    StringBuilder sb = new StringBuilder();
    for (ThrowableDataPoint tdp : tdpArray) {
      sb.append(tdp.toString());
      sb.append(Layout.LINE_SEP);
    }
    String expected = sw.toString();
    String result = sb.toString().replace("common frames omitted", "more");
    
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
