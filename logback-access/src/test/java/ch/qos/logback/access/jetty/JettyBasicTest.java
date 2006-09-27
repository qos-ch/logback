package ch.qos.logback.access.jetty;


import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.Util;
import ch.qos.logback.core.appender.ListAppender;

public class JettyBasicTest extends TestCase {
  
  static RequestLogImpl requestLogImpl;
  
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(JettyBasicTest.class);
    requestLogImpl = new RequestLogImpl();
    return new JettyTestSetup(suite, requestLogImpl);
  }
  
//  public void testGetRequest() throws Exception {
//    URL url = new URL("http://localhost:8080/");
//    HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
//    connection.setDoInput(true);
//    
//    String result = Util.readToString(connection.getInputStream());
//    
//    assertEquals("hello world", result); 
//    
//    ListAppender appender = (ListAppender)requestLogImpl.getAppender("list");
//    appender.list.clear();
//  }
  
  public void testPostContentConverter() throws Exception {
    System.out.println("into test");
    URL url = new URL("http://localhost:8080/");
    HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
    ((HttpURLConnection)connection).setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setUseCaches(false);
    connection.setRequestProperty("Content-Type", "text/plain");
    
    String msg = "test message";
    PrintWriter output = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()));
    output.print(msg);
    output.flush();
    output.close();
    
    String result = Util.readToString(connection.getInputStream());
    
    ListAppender appender = (ListAppender)requestLogImpl.getAppender("list");
    //assertEquals(1, appender.list.size());
    Iterator it = appender.list.iterator();
    int i = 0;
    while(it.hasNext()) {
      AccessEvent event = (AccessEvent)it.next();
      System.out.println(i++ + ": " + event.getPostContent());
    }
    //System.out.println("0: " + event.getPostContent());
    
  }

}
