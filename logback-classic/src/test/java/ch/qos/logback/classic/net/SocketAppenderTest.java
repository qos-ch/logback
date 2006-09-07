package ch.qos.logback.classic.net;

import java.util.Map;

import junit.framework.TestCase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextRemoteView;
import ch.qos.logback.classic.spi.LoggerRemoteView;
import ch.qos.logback.classic.spi.LoggingEvent;

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
		lc.setName("test");
		lc.setProperty("testKey", "testValue");
		Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
		SocketAppender socketAppender = new SocketAppender();
		socketAppender.setContext(lc);
		socketAppender.setName("socket");
		socketAppender.setPort(4560);
		socketAppender.setRemoteHost("localhost");
		root.addAppender(socketAppender);
		socketAppender.start();
				
		Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
		logger.debug("test msg");

    // Wait max 2 seconds for mock server to finish. However, it should
    // finish much sooner than that.		
		mockServer.join(2000);
		assertTrue(mockServer.finished);
		assertEquals(1, mockServer.loggingEventList.size());
		LoggingEvent remoteEvent = mockServer.loggingEventList.get(0);
		LoggerRemoteView loggerRemoteView = remoteEvent.getLoggerRemoteView();
		assertEquals("test msg", remoteEvent.getMessage());
		assertNotNull(loggerRemoteView);
		assertEquals("root", loggerRemoteView.getName());

		LoggerContextRemoteView loggerContextRemoteView = loggerRemoteView.getLoggerContextView();
		assertNotNull(loggerContextRemoteView);
		assertEquals("test", loggerContextRemoteView.getName());
		Map<String, String> props = loggerContextRemoteView.getPropertyMap();
		assertEquals("testValue", props.get("testKey"));
	}
}
