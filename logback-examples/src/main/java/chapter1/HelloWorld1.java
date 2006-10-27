package chapter1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorld1 {

	public static void main(String[] args) {

    Logger logger = LoggerFactory.getLogger("chapter1.HelloWorld1");
		logger.debug("Hello world.");
	}
}
