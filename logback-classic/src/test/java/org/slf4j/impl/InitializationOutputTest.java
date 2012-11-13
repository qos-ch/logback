package org.slf4j.impl;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.status.NopStatusListener;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.util.TeeOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class InitializationOutputTest {

  int diff = RandomUtil.getPositiveInt();

  TeeOutputStream tee;
  PrintStream original;

  @Before
  public void setUp()  {
    original = System.out;
    // tee will output bytes on System out but it will also
    // collect them so that the output can be compared against
    // some expected output data

    // keep the console quiet
    tee = new TeeOutputStream(null);

    // redirect System.out to tee
    System.setOut(new PrintStream(tee));
  }

  @After
  public void tearDown()  {
    System.setOut(original);
    System.clearProperty(ContextInitializer.CONFIG_FILE_PROPERTY);
    System.clearProperty(ContextInitializer.STATUS_LISTENER_CLASS);
  }


  @Test
  public void noOutputIfContextHasAStatusListener() {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, ClassicTestConstants.INPUT_PREFIX + "issue/logback292.xml");
    System.setProperty(ContextInitializer.STATUS_LISTENER_CLASS, NopStatusListener.class.getName());

    StaticLoggerBinderFriend.reset();
    assertEquals(0, tee.baos.size());
  }

}
