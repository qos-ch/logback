package ch.qos.logback.classic.issue.logback1159;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;

//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerFactoryFriend;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackListenerTest {
	private final File logFile = new File("target/test.log");

	private void doConfigure() throws JoranException {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		configurator.doConfigure(new File("src/test/input/issue/logback-1159.xml"));
	}

	@After
	public void after() {
		logFile.delete();
		LoggerFactoryFriend.reset();
	}

	private void disableLogFileAccess() throws IOException {
		logFile.createNewFile();
		logFile.deleteOnExit();
		final Path path = Paths.get(logFile.toURI());
		final Set<PosixFilePermission> permissions = Collections.emptySet();
		try {
			Files.setPosixFilePermissions(path, permissions);
		} catch (final UnsupportedOperationException e) {
			path.toFile().setReadOnly();
		}
	}

	@Test(expected = LoggingError.class)
	public void testThatErrorIsDetectedAtLogInit() throws Exception {
		disableLogFileAccess();
		doConfigure();
	}

	@Test
	public void assertThatNonFailSafeAppendersNotAffected() throws JoranException {
		doConfigure();
		final Logger logger = LoggerFactory.getLogger("NOTJOURNAL");
		logger.error("This should not fail");
	}

}