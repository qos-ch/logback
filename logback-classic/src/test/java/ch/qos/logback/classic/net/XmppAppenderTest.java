package ch.qos.logback.classic.net;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class XmppAppenderTest {

	@Test 
	public void testSimpleMessage() {
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.warn("Test message 22");
	}



	@Test 
	public void testMessageWithException() {
		Logger logger = LoggerFactory.getLogger(getClass());
		try {
			throw new Exception("Exception message.");
		} catch (Exception e) {
			logger.info("Exception text.", e);
		}
	}
	
	
	
	@After
	public void waitFor() throws InterruptedException {
        Thread.sleep(1800);
	}
}
