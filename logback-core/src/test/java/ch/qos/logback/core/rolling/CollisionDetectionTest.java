package ch.qos.logback.core.rolling;

import static ch.qos.logback.core.CoreConstants.FA_FILENAME_COLLISION_MAP;
import static ch.qos.logback.core.testUtil.CoreTestConstants.OUTPUT_DIR_PREFIX;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.NopEncoder;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.testUtil.RandomUtil;
import ch.qos.logback.core.testUtil.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;

public class CollisionDetectionTest {

	Context context = new ContextBase();
	StatusChecker statusChecker = new StatusChecker(context);
	int diff = RandomUtil.getPositiveInt();
	protected String randomOutputDir = OUTPUT_DIR_PREFIX + diff + "/";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	FileAppender<String> buildFileAppender(final String name, final String filenameSuffix) {
		final FileAppender<String> fileAppender = new FileAppender<>();
		fileAppender.setName(name);
		fileAppender.setContext(context);
		fileAppender.setFile(randomOutputDir+filenameSuffix);
		fileAppender.setEncoder(new NopEncoder<String>());
		return fileAppender;
	}

	RollingFileAppender<String> buildRollingFileAppender(final String name, final String filenameSuffix, final String patternSuffix) {
		final RollingFileAppender<String> rollingFileAppender = new RollingFileAppender<>();
		rollingFileAppender.setName(name);
		rollingFileAppender.setContext(context);
		rollingFileAppender.setFile(randomOutputDir+filenameSuffix);
		rollingFileAppender.setEncoder(new NopEncoder<String>());

		final TimeBasedRollingPolicy<String> tbrp = new TimeBasedRollingPolicy<>();
		tbrp.setContext(context);
		tbrp.setFileNamePattern(randomOutputDir+patternSuffix);
		tbrp.setParent(rollingFileAppender);
		//tbrp.timeBasedFileNamingAndTriggeringPolicy = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();
		//tbrp.timeBasedFileNamingAndTriggeringPolicy.setCurrentTime(givenTime);
		rollingFileAppender.setRollingPolicy(tbrp);
		tbrp.start();


		return rollingFileAppender;
	}


	@Test
	public void collisionImpossibleForSingleAppender() {
		final FileAppender<String> fileAppender = buildFileAppender("FA", "collisionImpossibleForSingleAppender");
		fileAppender.start();
		statusChecker.assertIsErrorFree();

	}

	@Test
	public void appenderStopShouldClearEntryInCollisionMap() {
		final String key = "FA";
		final FileAppender<String> fileAppender = buildFileAppender(key, "collisionImpossibleForSingleAppender");
		fileAppender.start();
		assertCollisionMapHasEntry(FA_FILENAME_COLLISION_MAP, key);
		fileAppender.stop();
		assertCollisionMapHasNoEntry(FA_FILENAME_COLLISION_MAP, key);
		statusChecker.assertIsErrorFree();


	}

	private void assertCollisionMapHasEntry(final String mapName, final String key) {
		@SuppressWarnings("unchecked")
		final
		Map<String, ?> map = (Map<String, ?>) context.getObject(mapName);
		Assert.assertNotNull(map);
		Assert.assertNotNull(map.get(key));
	}
	private void assertCollisionMapHasNoEntry(final String mapName, final String key) {
		@SuppressWarnings("unchecked")
		final
		Map<String, ?> map = (Map<String, ?>) context.getObject(mapName);
		Assert.assertNotNull(map);
		Assert.assertNull(map.get(key));
	}

	@Test
	public void collisionWithTwoFileAppenders() {
		final String suffix = "collisionWithToFileAppenders";

		final FileAppender<String> fileAppender1 = buildFileAppender("FA1", suffix);
		fileAppender1.start();

		final FileAppender<String> fileAppender2 = buildFileAppender("FA2", suffix);
		fileAppender2.start();
		statusChecker.assertContainsMatch(Status.ERROR, "'File' option has the same value");
		//StatusPrinter.print(context);
	}

	@Test
	public void collisionWith_FA_RFA() {
		final String suffix = "collisionWith_FA_RFA";

		final FileAppender<String> fileAppender1 = buildFileAppender("FA", suffix);
		fileAppender1.start();

		final RollingFileAppender<String> rollingfileAppender = buildRollingFileAppender("RFA", suffix, "bla-%d.log");
		rollingfileAppender.start();
		StatusPrinter.print(context);
		statusChecker.assertContainsMatch(Status.ERROR, "'File' option has the same value");
	}

	@Test
	public void collisionWith_2RFA() {
		final String suffix = "collisionWith_2RFA";

		final RollingFileAppender<String> rollingfileAppender1 = buildRollingFileAppender("RFA1", suffix, "bla-%d.log");
		rollingfileAppender1.start();
		final RollingFileAppender<String> rollingfileAppender2 = buildRollingFileAppender("RFA1", suffix, "bla-%d.log");
		rollingfileAppender2.start();

		StatusPrinter.print(context);
		statusChecker.assertContainsMatch(Status.ERROR, "'FileNamePattern' option has the same value");
	}

}
