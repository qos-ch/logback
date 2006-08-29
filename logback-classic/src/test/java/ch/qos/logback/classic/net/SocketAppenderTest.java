package ch.qos.logback.classic.net;

import junit.framework.TestCase;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.appender.ListAppender;

public class SocketAppenderTest extends TestCase{
	
	LoggerContext lc;
	ListAppender remoteListAppender;
	
	public void testStartFailNoRemoteHost() {
		LoggerContext lc = new LoggerContext();
		SocketAppender appender = new SocketAppender();
		appender.setContext(lc);
		appender.setPort(123);
		appender.start();
		assertEquals(1, lc.getStatusManager().getCount());
	}
	
	public void testRecieveMessage() {
		Logger logger = lc.getLogger(LoggerContext.ROOT_NAME);
		logger.debug("test");
		assertEquals(1, remoteListAppender.list.size());
	}
	
	public void setUp() {
		//client configuration
		lc = new LoggerContext();
		Logger root = lc.getLogger(LoggerContext.ROOT_NAME);
		SocketAppender socket = new SocketAppender();
		socket.setContext(lc);
		socket.setName("socket");
		socket.setPort(4560);
		socket.setRemoteHost("localhost");
		root.addAppender(socket);
		
		//server configuration
		LoggerContext remoteLc = ((Logger)SimpleSocketServer.logger).getLoggerContext();
		Logger remoteRoot = remoteLc.getLogger(LoggerContext.ROOT_NAME);
		remoteListAppender = new ListAppender();
		remoteListAppender.setContext(remoteLc);
		remoteListAppender.setName("list");
		remoteRoot.addAppender(remoteListAppender);
		SimpleSocketServer.runServer();
	}
	
	public void tearDown() {
		lc = null;
	}
}
