package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.util.LoggerStatusPrinter;

public class HelloWorld2 {

	public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger("chapter1.HelloWorld2");
		logger.debug("Hello world.");
		LoggerStatusPrinter.printStatusInDefaultContext();
	}
}
