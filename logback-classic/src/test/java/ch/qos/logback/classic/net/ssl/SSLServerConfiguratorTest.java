package ch.qos.logback.classic.net.ssl;

import org.junit.Test;

import ch.qos.logback.classic.net.server.LogbackSocketServer;

public class SSLServerConfiguratorTest {

  @Test
  public void testCreateServer() throws Exception {
    LogbackSocketServer.main(new String[] { "ssl/testServer.xml" }); 
    Thread.sleep(Long.MAX_VALUE);
  }
}
