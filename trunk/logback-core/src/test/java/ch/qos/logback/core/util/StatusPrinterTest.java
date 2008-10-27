package ch.qos.logback.core.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;

public class StatusPrinterTest extends TestCase {

  ByteArrayOutputStream outputStream;
  PrintStream ps;
  
  public StatusPrinterTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    outputStream = new ByteArrayOutputStream();
    ps = new PrintStream(outputStream);
    StatusPrinter.setPrintStream(ps);
    super.setUp();
  }

  protected void tearDown() throws Exception {
    StatusPrinter.setPrintStream(System.out);
    ps = null;
    outputStream = null;
    super.tearDown();
  }
  
  public void testBasic() {
    Context context = new ContextBase();
    context.getStatusManager().add(new InfoStatus("test", this));
    StatusPrinter.print(context);
    String result = outputStream.toString();
    assertTrue(result.contains("|-INFO in testBasic"));
  }

  public void testNested() {
    Status s0 = new ErrorStatus("test0", this);
    Status s1 = new InfoStatus("test1", this);
    Status s11 = new InfoStatus("test11", this);
    Status s12 = new InfoStatus("test12", this);
    s1.add(s11);
    s1.add(s12);
    
    Status s2 = new InfoStatus("test2", this);
    Status s21 = new InfoStatus("test21", this);
    Status s211 = new WarnStatus("test211", this);
    
    Status s22 = new InfoStatus("test22", this);
    s2.add(s21);
    s2.add(s22);
    s21.add(s211);
    
    
    Context context = new ContextBase();
    context.getStatusManager().add(s0);
    context.getStatusManager().add(s1);
    context.getStatusManager().add(s2);

    StatusPrinter.print(context);
    String result = outputStream.toString();
    assertTrue(result.contains("+ INFO in testNested"));
    assertTrue(result.contains("+ WARN in testNested"));
    assertTrue(result.contains("    |-WARN in testNested"));
  }
  
  public void testWithException() {
    Status s0 = new ErrorStatus("test0", this);
    Status s1 = new InfoStatus("test1", this, new Exception("testEx"));
    Status s11 = new InfoStatus("test11", this);
    Status s12 = new InfoStatus("test12", this);
    s1.add(s11);
    s1.add(s12);
    
    Status s2 = new InfoStatus("test2", this);
    Status s21 = new InfoStatus("test21", this);
    Status s211 = new WarnStatus("test211", this);
    
    Status s22 = new InfoStatus("test22", this);
    s2.add(s21);
    s2.add(s22);
    s21.add(s211);
    
    Context context = new ContextBase();
    context.getStatusManager().add(s0);
    context.getStatusManager().add(s1);
    context.getStatusManager().add(s2);
    StatusPrinter.print(context);  
    String result = outputStream.toString();
    assertTrue(result.contains("|-ERROR in testWithException"));
    assertTrue(result.contains("+ INFO in testWithException"));
    assertTrue(result.contains("ch.qos.logback.core.util.StatusPrinterTest.testWithException"));
  }
  
}
