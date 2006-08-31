package ch.qos.logback.classic.net;

import junit.framework.TestCase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class SocketAppenderTest extends TestCase {

	public void testStartFailNoRemoteHost() {
		LoggerContext lc = new LoggerContext();
		SocketAppender appender = new SocketAppender();
		appender.setContext(lc);
		appender.setPort(123);
		appender.start();
		assertEquals(1, lc.getStatusManager().getCount());
	}

	public void testRecieveMessage() throws InterruptedException {
		MockSocketServer mockServer = new MockSocketServer(1);
		mockServer.start();
		//mockServer.join(1000);
		
		// client configuration
		LoggerContext lc = new LoggerContext();
		Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
		SocketAppender socketAppender = new SocketAppender();
		socketAppender.setContext(lc);
		socketAppender.setName("socket");
		socketAppender.setPort(4560);
		socketAppender.setRemoteHost("localhost");
		root.addAppender(socketAppender);
		socketAppender.start();
		
		Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
		logger.debug("test");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.		
		mockServer.join(2000);
		assertTrue(mockServer.finished);
		assertTrue(mockServer.msgList.get(0).startsWith("ch.qos.logback.classic.spi.LoggingEvent@"));
		
	}
}
