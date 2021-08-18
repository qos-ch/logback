package ch.qos.logback.classic.spi;

import static org.junit.Assert.assertTrue;

import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.testUtil.StringPrintStream;

public class InvocationTest {

	private final PrintStream oldErr = System.err;
	final String loggerName = this.getClass().getName();
	StringPrintStream sps = new StringPrintStream(oldErr, true);

	@Before
    public void setUp() throws Exception {
        System.setErr(sps);
    }
    
    @After
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
		
		assertTrue(sps.stringList.isEmpty());
		
	}

}
