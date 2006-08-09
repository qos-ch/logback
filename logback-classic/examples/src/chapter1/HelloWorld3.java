package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.BasicConfigurator;
import ch.qos.logback.classic.util.LoggerStatusPrinter;

public class HelloWorld3 {

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(HelloWorld3.class);

		BasicConfigurator.configureDefaultContext();
		logger.debug("Hello world.");
		LoggerStatusPrinter.printStatusInDefaultContext();
	}
}
