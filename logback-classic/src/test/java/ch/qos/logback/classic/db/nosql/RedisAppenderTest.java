package ch.qos.logback.classic.db.nosql;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.pattern.util.IEscapeUtil;
import ch.qos.logback.core.testUtil.RandomUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * Tests for {@link RedisAppenderBase}
 *
 * TODO better tests pending Jedis interface for stubbing, and/or in-memory Redis server.
 *
 * @author Juan Uys
 */
public class RedisAppenderTest {

  LoggerContext lc;
  Logger logger;
  RedisAppenderBase appender;

  int diff = RandomUtil.getPositiveInt();

  @Before
  public void setUp() {
    lc = new LoggerContext();
    lc.setName("default");
    logger = lc.getLogger("root");
    appender = new RedisAppender();
    appender.setName("Redis");
    appender.setContext(lc);

    appender.setHostName("localhost");
    //appender.start();
  }

  @After
  public void tearDown() {
    //appender.stop();
    logger = null;
    lc = null;
    appender = null;
  }

  @Test
  public void testSave() {
    ILoggingEvent event = createLoggingEvent();

    //appender.append(event);
    // TODO etc
  }

  private ILoggingEvent createLoggingEvent() {
    ILoggingEvent le = new LoggingEvent(this.getClass().getName(), logger,
        Level.DEBUG, "test message", new Exception("test Ex"), new Integer[]{diff});
    return le;
  }
}