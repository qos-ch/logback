package ch.qos.logback.classic.net;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.Constants;

public class SocketAppenderTest {


	public static void main(String[] args) {
		
//		Thread t = new Thread(new Runnable() {
//			public void run() {
//				SimpleSocketServer.main(new String[]{"4560", Constants.TEST_DIR_PREFIX + "input/socket/serverConfig.xml"});		
//			}
//		});
		
//		t.start();
		
		Logger logger = (Logger) LoggerFactory.getLogger(SocketAppenderTest.class);
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		configurator.doConfigure(Constants.TEST_DIR_PREFIX + "input/socket/clientConfig.xml");
			
		logger.debug("************* Hello world.");
		
//		t.interrupt();
//		System.exit(0);
		
	}

}
