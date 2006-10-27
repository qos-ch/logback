package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Bar {
	Logger logger = LoggerFactory.getLogger(Bar.class);
	public void doIt() {
		logger.debug("doing my job");
	}
}