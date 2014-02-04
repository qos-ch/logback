package ch.qos.logback.classic.net;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketAppenderMessageLossTest {
  int runLen = 1000;

  @Test(timeout = 1000)
  public void synchronousSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  @Test(timeout = 1000)
  public void smallQueueSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setQueueSize(10);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  @Test(timeout = 1000)
  public void largeQueueSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setQueueSize(runLen*5);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  static public class ListAppenderWithLatch extends AppenderBase<ILoggingEvent> {
    public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
    CountDownLatch latch;

    ListAppenderWithLatch(CountDownLatch latch) {
      this.latch = latch;
    }
    protected void append(ILoggingEvent e) {
      list.add(e);
      latch.countDown();
   }
  }


  public void runTest(SocketAppender socketAppender) throws Exception {
    final int port = RandomUtil.getRandomServerPort();



    LoggerContext serverLoggerContext = new LoggerContext();
    serverLoggerContext.setName("serverLoggerContext");

    CountDownLatch allMessagesReceivedLatch = new CountDownLatch(runLen);
    ListAppenderWithLatch listAppender = new ListAppenderWithLatch(allMessagesReceivedLatch);
    listAppender.setContext(serverLoggerContext);
    listAppender.start();

    Logger serverLogger = serverLoggerContext.getLogger(getClass());
    serverLogger.setAdditive(false);
    serverLogger.addAppender(listAppender);


    LoggerContext loggerContext = new LoggerContext();
    loggerContext.setName("clientLoggerContext");
    socketAppender.setContext(loggerContext);

    CountDownLatch latch = new  CountDownLatch(1);
    SimpleSocketServer simpleSocketServer =  new SimpleSocketServer(serverLoggerContext, port);
    simpleSocketServer.start();
    simpleSocketServer.setLatch(latch);

    latch.await();

    socketAppender.setPort(port);
    socketAppender.setRemoteHost("localhost");
    socketAppender.setReconnectionDelay(1000);
    socketAppender.setIncludeCallerData(true);
    socketAppender.start();
    assertTrue(socketAppender.isStarted());

    Logger logger = loggerContext.getLogger(getClass());
    logger.setAdditive(false);
    logger.addAppender(socketAppender);


    for (int i = 0; i < runLen; ++i) {
      logger.info("hello");
    }

    allMessagesReceivedLatch.await();

    assertEquals(runLen, listAppender.list.size());
    loggerContext.stop();
    simpleSocketServer.close();
  }
}
