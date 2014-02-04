package ch.qos.logback.classic.net;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketAppenderLostMessagesTest {

  @Test
  public void testSynchronousSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  @Test
  public void testSmallQueueSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setQueueSize(25);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  @Test
  public void testLargeQueueSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setQueueSize(25000);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  public void runTest(SocketAppender socketAppender) throws Exception {
    final int port = RandomUtil.getRandomServerPort();

    LoggerContext serverLoggerContext = new LoggerContext();
    serverLoggerContext.setName("serverLoggerContext");

    ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
    listAppender.setContext(serverLoggerContext);
    listAppender.start();

    Logger serverLogger = serverLoggerContext.getLogger(getClass());
    serverLogger.setAdditive(false);
    serverLogger.addAppender(listAppender);


    LoggerContext loggerContext = new LoggerContext();
    loggerContext.setName("clientLoggerContext");
    socketAppender.setContext(loggerContext);


    SimpleSocketServer simpleSocketServer =  new SimpleSocketServer(serverLoggerContext, port);
    simpleSocketServer.start();

    Thread.sleep(1000);

    socketAppender.setPort(port);
    socketAppender.setRemoteHost("localhost");
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setIncludeCallerData(true);
    socketAppender.start();
    assertTrue(socketAppender.isStarted());

    Logger logger = loggerContext.getLogger(getClass());
    logger.setAdditive(false);
    logger.addAppender(socketAppender);


    for (int i = 0; i < 10000; ++i) {
      logger.info("hello");
    }

    Thread.sleep(1000);

    assertEquals(10000, listAppender.list.size());
    loggerContext.stop();
    simpleSocketServer.close();
  }
}
