package chapters.appenders.socket;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SocketAppender;
import ch.qos.logback.core.status.OnConsoleStatusListener;

/**
 * Created with IntelliJ IDEA. User: ceki Date: 27.06.12 Time: 19:35 To change
 * this template use File | Settings | File Templates.
 */
public class ConsolePluginClient {

	static public void main(String[] args) throws Exception {
		// Create a SocketAppender connected to hostname:port with a
		// reconnection delay of 10000 seconds.
		String hostname = "localhost";
		int port = 4321;
		SocketAppender socketAppender = new SocketAppender();
		socketAppender.setRemoteHost(hostname);
		socketAppender.setPort(port);
		socketAppender.setIncludeCallerData(true);
		socketAppender.setReconnectionDelay(10000);

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		socketAppender.setContext(lc);

		lc.reset();

		lc.getStatusManager().add(new OnConsoleStatusListener());
		// SocketAppender options become active only after the execution
		// of the next statement.
		socketAppender.start();

		Logger logger = (Logger) LoggerFactory
				.getLogger(ConsolePluginClient.class);
		logger.addAppender(socketAppender);

		UglyBetty ub = new UglyBetty("ugly-better-thread-234");
		ub.start();
		for (int i = 0; i < 1000; i++) {
			if (i % 3 == 0) {
				logger.warn(i + " is divisible by 3");
			} else {
				toto(logger, i);
			}
			Thread.sleep(50);
		}
		ub.join();

	}

	static void toto(Logger logger, int i) {
		logger.debug("this is message number " + i);
	}

	static class UglyBetty extends Thread {
		org.slf4j.Logger logger = LoggerFactory.getLogger(UglyBetty.class);

		public UglyBetty(String name) {
			super(name);
		}

		public void run() {
			for (int i = 0; i < 1000; i++) {
				if (i % 5 == 0) {
					logger.warn(i + " is divisible by 5");
				} else if (i % 47 == 0) {
					logger.error("this is an exception", new Exception("test"));
				} else {
					logger.debug("this is message number " + i);
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
