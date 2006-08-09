package chapter1;

// Import SLF4J classes.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.BasicConfigurator;

public class MyApp {

	public static void main(String[] args) {
		// Set up a simple configuration that logs on the console.
		BasicConfigurator.configureDefaultContext();

		Logger logger = LoggerFactory.getLogger(MyApp.class);
		
		logger.info("Entering application.");
		Bar bar = new Bar();
		bar.doIt();
		logger.info("Exiting application.");
	}
}
