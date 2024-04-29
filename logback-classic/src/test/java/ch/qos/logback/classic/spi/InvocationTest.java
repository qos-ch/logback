package ch.qos.logback.classic.spi;

import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.testUtil.StringPrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvocationTest {

    private final PrintStream oldErr = System.err;
    final String loggerName = this.getClass().getName();
    StringPrintStream sps = new StringPrintStream(oldErr, true);

    String CONNECTED_WITH_MESSAGE = "SLF4J(I): Connected with provider of type [ch.qos.logback.classic.spi.LogbackServiceProvider]";

    @BeforeEach
    public void setUp() throws Exception {
        System.setErr(sps);
    }

    @AfterEach
    public void tearDown() throws Exception {
        LoggerFactoryFriend.reset();
        System.setErr(oldErr);
    }

    // https://jira.qos.ch/browse/LOGBACK-1568 would have been prevented
    // had this silly test existed.
    @Test
    public void smoke() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Hello world.");

        assertEquals(1, sps.stringList.size());
        assertEquals(CONNECTED_WITH_MESSAGE, sps.stringList.get(0));

    }

}
