package ch.qos.logback.classic.pattern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.ClassicTestConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.testUtil.Gaussian;
import ch.qos.logback.core.status.OnConsoleStatusListener;

@Ignore
public class LoggerNameConverterPerfTest {

	static final String NAMES_FILE = ClassicTestConstants.INPUT_PREFIX + "fqcn.txt";

	static List<String> NAMES_LIST;

	static int SIZE;
	static double MEAN;
	static double DEVIATION;
	static Gaussian G;

	LoggerContext loggerContext = new LoggerContext();
	LoggerConverter loggerConverter = new LoggerConverter();
	
	LoggerNameOnlyLoggingEvent event = new LoggerNameOnlyLoggingEvent();

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@BeforeClass
	static public void loadClassNames() throws IOException {

		NAMES_LIST = Files.lines(Paths.get(NAMES_FILE)).collect(Collectors.toList());

		SIZE = NAMES_LIST.size();
		MEAN = SIZE / 2;
		DEVIATION = MEAN / 8;
		G = new Gaussian(MEAN, DEVIATION);
		System.out.println("names list size=" + SIZE);
	}

	@Before
	public void setUp() {
		OnConsoleStatusListener ocsl = new OnConsoleStatusListener();
		ocsl.setContext(loggerContext);
		ocsl.start();
		loggerContext.getStatusManager().add(ocsl);
		loggerConverter.setOptionList(Arrays.asList("30"));
		loggerConverter.setContext(loggerContext);
		loggerConverter.start();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void measureAbbreviationPerf() {
		for(int i = 0; i < 10*1000; i++) {
			performAbbreviation();
		}
		for(int i = 0; i < 10*1000; i++) {
			performAbbreviation();
		}
		final int runLength = 1000*1000;
		System.out.println("Start measurements");
		long start = System.nanoTime();
		for(int i = 0; i < runLength; i++) {
			performAbbreviation();
		}
		long end = System.nanoTime();
		long diff = end - start;
		double average = diff*1.0D/runLength;
		logger.atInfo().addArgument(average).log("Average = {} nanos");
		int cacheMisses = loggerConverter.getCacheMisses();
		
		logger.atInfo().addArgument(cacheMisses).log("cacheMisses = {} ");
		logger.atInfo().addArgument(runLength).log("total calls= = {} ");
		
		double cacheMissRate = loggerConverter.getCacheMissRate()*100;
		logger.atInfo().addArgument(cacheMissRate).log("cacheMiss rate %= {} ");
		
	}

	
	public void performAbbreviation() {
		String fqn = getFQN();
		event.setLoggerName(fqn);
		loggerConverter.convert(event);
	}
	
	private String getFQN() {
		while (true) {
			int index = (int) G.getGaussian();
			if (index >= 0 && index < SIZE) {
				return NAMES_LIST.get(index);
			} else {
				continue;
			}
		}
	}

}
