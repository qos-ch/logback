package ch.qos.logback.classic.net;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.Duration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SocketAppenderMessageLossTest {
  int runLen = 100;
  Duration reconnectionDelay =  new Duration(1000);

  @Test(timeout = 1000)
  public void synchronousSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(reconnectionDelay);
    socketAppender.setIncludeCallerData(true);

    runTest(socketAppender);
  }

  @Test(timeout = 1000)
  public void smallQueueSocketAppender() throws Exception {

    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(reconnectionDelay);
    socketAppender.setQueueSize(runLen/10);

    runTest(socketAppender);
  }

  @Test(timeout = 1000)
  public void largeQueueSocketAppender() throws Exception {
    SocketAppender socketAppender = new SocketAppender();
    socketAppender.setReconnectionDelay(reconnectionDelay);
    socketAppender.setQueueSize(runLen*5);

    runTest(socketAppender);
  }

  // appender used to signal when the N'th event (as set in the latch) is received by the server
  // this allows us to have test which are both more robust and quicker.
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
    socketAppender.setReconnectionDelay(reconnectionDelay);
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
